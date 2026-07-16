package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.dtos.OptInChannel;
import code.adagedo.proxialertengine.dtos.OptInStatus;
import code.adagedo.proxialertengine.dtos.eonets.EonetPayload;
import code.adagedo.proxialertengine.dtos.eonets.Events;
import code.adagedo.proxialertengine.dtos.eonets.Geometry;
import code.adagedo.proxialertengine.dtos.notification.disaster_alert.NotificationEvent;
import code.adagedo.proxialertengine.models.NotificationSetting;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.producer.EventProducer;
import code.adagedo.proxialertengine.repositories.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class DisasterEventServiceTest {


    @Mock
    private ProximityAlertService proximityAlertService;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private SetOperations<String, String> setOperations;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EventProducer eventProducer;

    @Mock
    private EonetPayload eonetPayload;

    @InjectMocks
    private DisasterEventService disasterEventService;

    private static final String EMAIL_TOPIC = "test_email_topic";
    private static final String SMS_TOPIC = "test_sms_topic";

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should immediately skip processing if eonet payload contains no events")
    public void processAndSendDisasterAlert_WhenNoEvents_ShouldDoNothing(){

        when(eonetPayload.events()).thenReturn(Collections.emptyList());

        disasterEventService.processAndSendDisasterAlertToKafkaTopic(eonetPayload);

        verifyNoInteractions(redisTemplate, proximityAlertService, notificationRepository, eventProducer);
    }

    @Test
    @DisplayName("Should skip event processing when event has no geometries")
    public void processAndSendDisasterAlert_WhenGeometriesAreEmpty_ShouldSkipEvent() {

        Events mockEvent = mock(Events.class);

        when(eonetPayload.events()).thenReturn(List.of(mockEvent));
        when(mockEvent.geometries()).thenReturn(Collections.emptyList());

        disasterEventService.processAndSendDisasterAlertToKafkaTopic(eonetPayload);

        verifyNoInteractions(redisTemplate, proximityAlertService, notificationRepository, eventProducer);
    }

    @Test
    @DisplayName("Should skip event processing if the event is already known/processed in Redis")
    public void processAndSendDisasterAlert_WhenEventIsDuplicateInRedis_ShouldSkip() {
        Events mockEvent = mock(Events.class);
        Geometry mockGeometry = mock(Geometry.class);

        when(eonetPayload.events()).thenReturn(List.of(mockEvent));
        when(mockEvent.geometries()).thenReturn(List.of(mockGeometry));
        when(mockEvent.id()).thenReturn("EONET_1234");
        when(mockGeometry.date()).thenReturn("2026-07-16T12:00:00Z");

        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.add("proxy_sentry:known_events", "EONET_1234_2026-07-16T12:00:00Z")).thenReturn(0L);

        disasterEventService.processAndSendDisasterAlertToKafkaTopic(eonetPayload);

        verifyNoInteractions(proximityAlertService, notificationRepository, eventProducer);
    }

    @Test
    @DisplayName("Should exit early if no users are found within the disaster's proximity radius")
    public void processAndSendDisasterAlert_WhenNoUsersInProximity_ShouldReturnEarly() {
        Events mockEvent = mock(Events.class, RETURNS_DEEP_STUBS);
        Geometry mockGeometry = mock(Geometry.class);

        when(eonetPayload.events()).thenReturn(List.of(mockEvent));
        when(mockEvent.geometries()).thenReturn(List.of(mockGeometry));
        when(mockEvent.id()).thenReturn("EONET_1234");
        when(mockGeometry.date()).thenReturn("2026-07-16T12:00:00Z");

        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.add("proxy_sentry:known_events", "EONET_1234_2026-07-16T12:00:00Z")).thenReturn(1L);

        when(mockGeometry.coordinates()).thenReturn(List.of(-74.0060, 40.7128));
        when(mockEvent.categories().getFirst().title()).thenReturn("Severe Storms");

        when(proximityAlertService.processUsersToSendDisasterAlert(eq(BigDecimal.valueOf(40.7128)),
                eq(BigDecimal.valueOf(-74.0060)),
                eq(350.0)))
                .thenReturn(Collections.emptyList());

        disasterEventService.processAndSendDisasterAlertToKafkaTopic(eonetPayload);

        verifyNoInteractions(notificationRepository, eventProducer);
    }

    @Test
    @DisplayName("Should successfully dispatch alerts to the correct Kafka topics matching user configurations")
    public void processAndSendDisasterAlert_ShouldRouteToCorrectTopicsBasedOnUserSettings() {

        Events mockEvent = mock(Events.class, RETURNS_DEEP_STUBS);
        Geometry mockGeometry = mock(Geometry.class);

        org.springframework.test.util.ReflectionTestUtils.setField(disasterEventService, "email_topic", EMAIL_TOPIC);
        org.springframework.test.util.ReflectionTestUtils.setField(disasterEventService, "sms_topic", SMS_TOPIC);

        when(eonetPayload.events()).thenReturn(List.of(mockEvent));
        when(mockEvent.geometries()).thenReturn(List.of(mockGeometry));
        when(mockEvent.id()).thenReturn("EONET_1234");
        when(mockGeometry.date()).thenReturn("2026-07-16T12:00:00Z");
        when(mockEvent.title()).thenReturn("Hurricane Mock");

        when(redisTemplate.opsForSet()).thenReturn(setOperations);
        when(setOperations.add("proxy_sentry:known_events", "EONET_1234_2026-07-16T12:00:00Z")).thenReturn(1L);

        when(mockGeometry.coordinates()).thenReturn(List.of(-74.0060, 40.7128));
        when(mockEvent.categories().getFirst().title()).thenReturn("Wildfires");

        User emailUser = User.builder().firstName("John").lastName("Email").email("john@email.com").phoneNumber("+111").build();
        User smsUser = User.builder().firstName("Jane").lastName("Sms").email("jane@sms.com").phoneNumber("+222").build();
        User bothUser = User.builder().firstName("Bob").lastName("Both").email("bob@both.com").phoneNumber("+333").build();

        when(proximityAlertService.processUsersToSendDisasterAlert(any(), any(), anyDouble()))
                .thenReturn(List.of(emailUser, smsUser, bothUser));

        NotificationSetting emailSetting = NotificationSetting.builder()
                .user(emailUser).channel(OptInChannel.EMAIL).optin_status(OptInStatus.SUBSCRIBED).build();
        NotificationSetting smsSetting = NotificationSetting.builder()
                .user(smsUser).channel(OptInChannel.SMS).optin_status(OptInStatus.SUBSCRIBED).build();
        NotificationSetting bothSetting = NotificationSetting.builder()
                .user(bothUser).channel(OptInChannel.BOTH).optin_status(OptInStatus.SUBSCRIBED).build();

        when(notificationRepository.findByUser(emailUser)).thenReturn(emailSetting);
        when(notificationRepository.findByUser(smsUser)).thenReturn(smsSetting);
        when(notificationRepository.findByUser(bothUser)).thenReturn(bothSetting);

        disasterEventService.processAndSendDisasterAlertToKafkaTopic(eonetPayload);

        verify(eventProducer, times(1)).publishEvents(any(NotificationEvent.class),
                eq(EMAIL_TOPIC),
                argThat(user -> "john@email.com".equals(user.getEmail()))
        );
        verify(eventProducer, never()).publishEvents(
                any(), eq(SMS_TOPIC),
                argThat(user -> "john@email.com".equals(user.getEmail()))
        );

        verify(eventProducer, times(1)).publishEvents(
                any(NotificationEvent.class),
                eq(SMS_TOPIC),
                argThat(user -> "jane@sms.com".equals(user.getEmail()))
        );
        verify(eventProducer, never()).publishEvents(
                any(),
                eq(EMAIL_TOPIC),
                argThat(user -> "jane@sms.com".equals(user.getEmail()))
        );

        verify(eventProducer, times(1)).publishEvents(
                any(NotificationEvent.class),
                eq(EMAIL_TOPIC),
                argThat(user -> "bob@both.com".equals(user.getEmail()))
        );
        verify(eventProducer, times(1)).publishEvents(
                any(NotificationEvent.class),
                eq(SMS_TOPIC),
                argThat(user -> "bob@both.com".equals(user.getEmail()))
        );
    }
}