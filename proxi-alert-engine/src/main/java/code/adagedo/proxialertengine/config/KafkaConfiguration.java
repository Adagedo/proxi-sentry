package code.adagedo.proxialertengine.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfiguration {

    @Value("${spring.kafka.topics.email_topic}")
    private String email_topic;

    @Value("${spring.kafka.topics.sms_topic}")
    private String sms_topic;

    @Value("${spring.kafka.topics.user_registered_topic}")
    private String user_registered;
    @Bean
    public NewTopic emailNotification(){

        return
                TopicBuilder.name(email_topic)
                        .partitions(3)
                        .replicas(3)
                        .build();

    }

    @Bean
    public NewTopic smsNotification() {

        return
                TopicBuilder
                        .name(sms_topic)
                        .partitions(3)
                        .replicas(3)
                        .build();
    }

    @Bean NewTopic userRegisteredNotification(){

        return
                TopicBuilder
                        .name(user_registered)
                        .partitions(3)
                        .replicas(3)
                        .build();
    }
}
