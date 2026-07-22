package code.adagedo.emailnotificationservice.service;

import code.adagedo.emailnotificationservice.dto.disaster_alert.NotificationEvent;
import code.adagedo.emailnotificationservice.dto.welcome.RegisterNotificationEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.internet.MimeMessage;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailService emailService;


    @Test
    @DisplayName("Should successfully process welcome notification and send email")
    public void shouldProcessWelcomeNotification() throws IOException {

        String payload = "{\"dummy\":\"data\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, "key", payload);

        RegisterNotificationEvent mockEvent = mock(RegisterNotificationEvent.class, RETURNS_DEEP_STUBS);

        when(mockEvent.eventType()).thenReturn("WELCOME");
        when(mockEvent.message()).thenReturn("Welcome to proxy sentry");
        when(mockEvent.subject()).thenReturn("Welcome");
        when(mockEvent.recipient().email()).thenReturn("user@gmail.com");
        when(mockEvent.recipient().name()).thenReturn("Lionel Messi");

        when(objectMapper.readValue(payload, RegisterNotificationEvent.class)).thenReturn(mockEvent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.processWelcomeNotification(record);

        verify(objectMapper, times(1)).readValue(payload, RegisterNotificationEvent.class);
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("Should throw exception when welcome notification processing fails")
    public void shouldThrowExceptionOnWelcomeNotificationFailure() throws IOException{
        String payload = "{\"dummy\":\"data\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, "key", payload);

        when(objectMapper.readValue(anyString(), eq(RegisterNotificationEvent.class))).thenThrow(new RuntimeException("JSON Parsing Error"));

        RuntimeException exception = assertThrows(
                RuntimeException.class, () -> emailService.processWelcomeNotification(record)
        );
        verify(mailSender, times(0)).createMimeMessage();
        assertEquals("JSON Parsing Error", exception.getMessage());
    }
    @Test
    @DisplayName("Should successfully process disaster alert notification and send email")
    public void shouldProcessDisasterAlertNotification() throws IOException {
        // Given
        String payload = "{\"dummy\":\"alert\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, "key", payload);

        NotificationEvent mockEvent = mock(NotificationEvent.class, RETURNS_DEEP_STUBS);
        when(mockEvent.eventType()).thenReturn("DISASTER_ALERT");
        when(mockEvent.payload().disasterName()).thenReturn("Hurricane Alpha");
        when(mockEvent.payload().disasterType()).thenReturn("HURRICANE");
        when(mockEvent.payload().distanceInKm()).thenReturn(15.5);
        when(mockEvent.payload().userName()).thenReturn("System");
        when(mockEvent.payload().latitude()).thenReturn(10.0);
        when(mockEvent.payload().longitude()).thenReturn(20.0);
        when(mockEvent.recipient().email()).thenReturn("alert@example.com");
        when(mockEvent.recipient().userName()).thenReturn("Jane Doe");
        when(mockEvent.recipient().phoneNumber()).thenReturn("+1234567890");

        when(objectMapper.readValue(payload, NotificationEvent.class)).thenReturn(mockEvent);
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.processDisasterAlertNotification(record);

        verify(objectMapper, times(1)).readValue(payload, NotificationEvent.class);
        verify(mailSender, times(1)).createMimeMessage();
        verify(mailSender, times(1)).send(mimeMessage);
    }
}