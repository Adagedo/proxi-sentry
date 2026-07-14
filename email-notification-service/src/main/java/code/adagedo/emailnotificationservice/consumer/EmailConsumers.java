package code.adagedo.emailnotificationservice.consumer;

import code.adagedo.emailnotificationservice.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmailConsumers {

    private final EmailService emailService;
    @KafkaListener(topics = {"email-disaster-alerts-notification"}, groupId = "email-listeners-group")
    public void ConsumerAlertNotification(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        log.info("alert consuming..., {}", consumerRecord);
        emailService.processDisasterAlertNotification(consumerRecord);
    }

    @KafkaListener(topics = {"proxy-sentry-user-registered"}, groupId = "email-listeners-group")
    public void ConsumerWelcomeNotification(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        log.info("welcome consuming..., {}", consumerRecord);
        emailService.processWelcomeNotification(consumerRecord);
    }
}
