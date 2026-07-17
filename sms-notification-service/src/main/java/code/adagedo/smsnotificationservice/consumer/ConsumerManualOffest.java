package code.adagedo.smsnotificationservice.consumer;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.jspecify.annotations.Nullable;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ConsumerManualOffest implements AcknowledgingMessageListener<String, String> {
    /**
     * Invoked with data from kafka.
     *
     * @param data           the data to be processed.
     * @param acknowledgment the acknowledgment.
     */
    @Override
    public void onMessage(ConsumerRecord<String, String> data, @Nullable Acknowledgment acknowledgment) {
        log.info("consumer record: {}",  data);
        assert acknowledgment != null;
        acknowledgment.acknowledge();
    }
}
