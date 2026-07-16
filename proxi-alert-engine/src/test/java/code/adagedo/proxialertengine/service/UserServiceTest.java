package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.dtos.OptInChannel;
import code.adagedo.proxialertengine.dtos.OptInStatus;
import code.adagedo.proxialertengine.dtos.notification.welcome_alert.RegisterNotificationEvent;
import code.adagedo.proxialertengine.dtos.request.OptInRequest;
import code.adagedo.proxialertengine.dtos.request.SubscriptionRequest;
import code.adagedo.proxialertengine.exceptions.ChannelNameException;
import code.adagedo.proxialertengine.exceptions.UserAlreadySubscribedException;
import code.adagedo.proxialertengine.exceptions.UserNotSubscribedException;
import code.adagedo.proxialertengine.models.NotificationSetting;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.producer.EventProducer;
import code.adagedo.proxialertengine.repositories.NotificationRepository;
import code.adagedo.proxialertengine.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;

import static org.mockito.ArgumentMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;


class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private EventProducer eventProducer;

    @InjectMocks
    private UserService userService;


    @Captor
    private ArgumentCaptor<NotificationSetting> notificationSettingCaptor;


    private static final String TOPIC = "test_user_registered_topic";
    private static final String EMAIL = "jane.doe@example.com";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should successfully register a new user and publish event")
    public void registerUser_WhenUserDoesNotExist_ShouldSaveAndPublish() {

        SubscriptionRequest request = new SubscriptionRequest(
                "Jane", "Adagedo",  EMAIL,"+1234567890"
               , BigDecimal.valueOf(3.4), BigDecimal.valueOf(5.6)
        );

        User savedUser = User.builder()
                .firstName("Jane")
                .lastName("Adagedo")
                .phoneNumber("+1234567890")
                .email(EMAIL)
                .longitude(BigDecimal.valueOf(3.4))
                .latitude(BigDecimal.valueOf(5.6))
                .build();

        org.springframework.test.util.ReflectionTestUtils.setField(userService, "user_registered", TOPIC);

        doReturn(null).when(userRepository).findByEmail(anyString());
        doReturn(savedUser).when(userRepository).save(any(User.class));

        User result = userService.registerUser(request);

        ArgumentCaptor<RegisterNotificationEvent> eventCaptor = ArgumentCaptor.forClass(RegisterNotificationEvent.class);
        ArgumentCaptor<String> topicCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);


        verify(userRepository).save(userCaptor.capture());
        User saved_user = userCaptor.getValue();
        assertEquals("Jane", saved_user.getFirstName());
        assertEquals(EMAIL, saved_user.getEmail());
        assertEquals(saved_user.getFirstName(), result.getFirstName());

        verify(notificationRepository).save(notificationSettingCaptor.capture());
        NotificationSetting savedSettings = notificationSettingCaptor.getValue();
        assertEquals(OptInChannel.BOTH, savedSettings.getChannel());
        assertEquals(OptInStatus.SUBSCRIBED, savedSettings.getOptin_status());
        assertEquals(saved_user.getFirstName(), savedSettings.getUser().getFirstName());

        verify(eventProducer).publishEvents(eventCaptor.capture(), topicCaptor.capture(), userCaptor.capture());
        RegisterNotificationEvent publishedEvent = eventCaptor.getValue();
        assertEquals(EMAIL, publishedEvent.recipient().email());
        assertEquals("Jane Adagedo", publishedEvent.recipient().name());
        assertEquals("Welcome to Proxy Sentry", publishedEvent.subject());
    }

    @Test
    @DisplayName("Should throw exception if user already exist")
    public void registerUser_WhenUserAlreadyExists_ShouldThrowException() {
        SubscriptionRequest request = new SubscriptionRequest(
                "Jane", "Adagedo",  EMAIL,"+1234567890"
                , BigDecimal.valueOf(3.4), BigDecimal.valueOf(5.6)
        );

        User existing_user = User.builder().email(EMAIL).build();
        doReturn(existing_user).when(userRepository).findByEmail(anyString());

        UserAlreadySubscribedException exception = assertThrows(
                UserAlreadySubscribedException.class, () -> {
                    userService.registerUser(request);
                }
        );
        assertEquals("user already subscribed", exception.getMessage());

        verify(userRepository, times(0)).save(any());
        verify(notificationRepository, times(0)).save(any());
        verify(eventProducer, times(0)).publishEvents(any(), any(), any());
    }

    @Test
    @DisplayName("Should create new notification settings when none exist")
    public void handleSubscription_WhenNoExistingSettings_ShouldCreateNew(){

        OptInRequest request = new OptInRequest(EMAIL, "BOTH");
        User existing_user = User.builder().email(EMAIL).build();

        NotificationSetting expectedSettings = NotificationSetting.builder()
                .user(existing_user)
                .channel(OptInChannel.BOTH)
                .optin_status(OptInStatus.SUBSCRIBED)
                .build();

        doReturn(existing_user).when(userRepository).findByEmail(anyString());
        doReturn(null).when(notificationRepository).findByUser(any());
        doReturn(expectedSettings).when(notificationRepository).save(any(NotificationSetting.class));

        NotificationSetting result = userService.handleSubscription(request, "SUBSCRIBED");
        NotificationSetting savedSettings = notificationRepository.save(expectedSettings);

        verify(notificationRepository, times(2)).save(any(NotificationSetting.class));

        assertEquals(OptInChannel.BOTH, savedSettings.getChannel());
        assertEquals(OptInStatus.SUBSCRIBED, savedSettings.getOptin_status());
        assertEquals(expectedSettings, result);
    }
    @Test
    @DisplayName("Should update existing notification settings when they already exist")
    void handleSubscription_WhenSettingsExist_ShouldUpdateExisting(){

        OptInRequest request = new OptInRequest(EMAIL, "SMS");
        User existing_user = User.builder().email(EMAIL).build();

        NotificationSetting existingSettings = NotificationSetting.builder()
                .user(existing_user)
                .channel(OptInChannel.BOTH)
                .optin_status(OptInStatus.SUBSCRIBED)
                .build();

        doReturn(existing_user).when(userRepository).findByEmail(anyString());
        doReturn(existingSettings).when(notificationRepository).findByUser(any());
        doReturn(existingSettings).when(notificationRepository).save(any(NotificationSetting.class));

        NotificationSetting result = userService.handleSubscription(request, "UNSUBSCRIBED");
        NotificationSetting update_settings = notificationRepository.save(existingSettings);

        verify(notificationRepository, times(2)).save(any(NotificationSetting.class));

        assertEquals(existingSettings, update_settings);
        assertEquals(OptInChannel.SMS, update_settings.getChannel());
        assertEquals(OptInStatus.UNSUBSCRIBED, update_settings.getOptin_status());
        assertEquals(update_settings, result);
    }

    @Test
    @DisplayName("Should throw exception if user is not found during subscription update")
    void handleSubscription_WhenUserNotFound_ShouldThrowException(){

        OptInRequest request = new OptInRequest("unknown@example.com", "BOTH");

        doReturn(null).when(userRepository).findByEmail(anyString());

        UserNotSubscribedException exception = assertThrows(
                UserNotSubscribedException.class, () -> {
                    userService.handleSubscription(request, "SUBSCRIBED");
                }
        );

        assertEquals("Please subscribe to perform this action", exception.getMessage());
        verify(notificationRepository, times(0)).save(any());

    }

    @Test
    @DisplayName("Should throw exception if channel is invalid")
    void handleSubscription_WhenChannelIsInvalid_ShouldThrowException(){

        OptInRequest request = new OptInRequest(EMAIL, "blah");
        User existingUser = User.builder().email(EMAIL).build();

        doReturn(existingUser).when(userRepository).findByEmail(anyString());

        ChannelNameException exception = assertThrows(
                ChannelNameException.class, () -> {
                    userService.handleSubscription(request, "SUBSCRIBED");
                }
        );

        assertEquals("invalid channel name", exception.getMessage());

        verify(notificationRepository, times(0)).save(any());
    }
}