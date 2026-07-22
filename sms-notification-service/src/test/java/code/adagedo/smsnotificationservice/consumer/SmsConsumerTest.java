package code.adagedo.smsnotificationservice.consumer;

import code.adagedo.smsnotificationservice.service.SmsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsConsumerTest {

    @Mock
    private SmsService smsService;

    @InjectMocks
    private SmsConsumer consumer;

    @Test
    @DisplayName("Should successfully consumer disaster alert and delegate it to the sms service")
    public void ShouldConsumerDisasterAlert() throws JsonProcessingException {

        String topic = "sms-disaster-alerts-notification";
        String key = "alert-123";

        String payload = "{\"alertId\":\"123\", \"severity\":\"HIGH\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>(topic, 0, 0L, key, payload);

        consumer.SmsAlertNotification(record);

        verify(smsService, times(1)).processAndSendSmsAlert(record);
        verifyNoMoreInteractions(smsService);
    }

}