package code.adagedo.emailnotificationservice.consumer;

import code.adagedo.emailnotificationservice.service.EmailService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
@ExtendWith(MockitoExtension.class)
class EmailConsumersTest {

    @Mock
    private EmailService emailService;

    @InjectMocks
    private EmailConsumers emailConsumers;

    @Test
    @DisplayName("Should successfully consume disaster alert notification and delegate to EmailService")
    public void shouldConsumeAlertNotification() throws IOException {
        String topic = "email-disaster-alerts-notification";
        String key = "alert-123";
        String payload = "{\"alertId\":\"123\", \"severity\":\"HIGH\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>(topic, 0, 0L, key, payload);

        emailConsumers.ConsumerAlertNotification(record);

        verify(emailService, times(1)).processDisasterAlertNotification(record);
        verifyNoMoreInteractions(emailService);
    }

    @Test
    @DisplayName("Should successfully consume welcome notification and delegate to EmailService")
    public void shouldConsumeWelcomeNotification() throws IOException {

        String topic = "proxy-sentry-user-registered";
        String key = "user-456";
        String payload = "{\"userId\":\"456\", \"email\":\"test@example.com\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>(topic, 0, 0L, key, payload);

        emailConsumers.ConsumerWelcomeNotification(record);

        verify(emailService, times(1)).processWelcomeNotification(record);

        verifyNoMoreInteractions(emailService);
    }
}