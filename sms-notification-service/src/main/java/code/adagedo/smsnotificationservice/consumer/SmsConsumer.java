package code.adagedo.smsnotificationservice.consumer;


import code.adagedo.smsnotificationservice.service.SmsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsConsumer {

    private final SmsService smsService;

    @KafkaListener(topics = {"sms-disaster-alerts-notification"}, groupId = " sms-listeners-group")
    public void SmsAlertNotification(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {

        log.info("alert consuming... {}", consumerRecord);

        smsService.processSmsAlert(consumerRecord);
    }
}
