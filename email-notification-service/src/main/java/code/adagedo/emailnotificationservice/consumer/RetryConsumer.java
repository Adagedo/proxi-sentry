package code.adagedo.emailnotificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class RetryConsumer {

    private static void accept(Header header) {
        log.info("key: {}, value: {}", header.key(), new String(header.value()));
    }

    @KafkaListener(topics = {"spring.kafka.topics.retry"}, autoStartup = "${retryListener.startup:true}", groupId = "retry-listener-group")
    public void welcomeRetry(ConsumerRecord<String, String> consumerRecord){
        log.info("welcome consumer record from retry topic: {}", consumerRecord);
        consumerRecord.headers().forEach(RetryConsumer::accept);
        // welcome email retry
    }
}
