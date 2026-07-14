package code.adagedo.emailnotificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
//@Component
public class EmailConsumers {

    @Value("${spring.kafka.topics.user_registered_topic}")
    private String welcome_email_topic;

    @Value("${spring.kafka.topics.disaster_email_topic}")
    private String disaster_email_topic;

//    @KafkaListener(topics = "email-disaster-alerts-notification", groupId = "email-listeners-group")
    public void ConsumerAlertNotification(ConsumerRecord<String, String> consumerRecord){

    }

//    @KafkaListener(topics = "proxy-sentry-user-registered", groupId = "email-listeners-group")
    public void ConsumerWelcomeNotification(ConsumerRecord<String, String> consumerRecord){

    }
}
