package code.adagedo.proxialertengine.producer;


import code.adagedo.proxialertengine.models.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.internals.RecordHeader;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.RecordComponent;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Slf4j
@RequiredArgsConstructor
public class EventProducer {


    private final KafkaTemplate<String, String> kafkaTemplate;

    private final ObjectMapper objectMapper;

    @Getter
    private static final String HEADER_EVENT_SOURCE = "event-source";
    @Getter
    private static final String SOURCE_SCANNER = "scanner";

    public <T> void publishEvents(T events, String topic, User user){

        if (events.getClass().isRecord()){
            RecordComponent[] components = events.getClass().getRecordComponents();
            String key = buildKey(user);
            String value = objectMapper.writeValueAsString(events);

            ProducerRecord<String, String> producerRecord = buildProducerRecord(key, value, topic);
            CompletableFuture<SendResult<String, String>> completableFuture = kafkaTemplate.send(producerRecord);
            completableFuture
                    .whenComplete((stringStringSendResult, throwable) -> {
                        if (throwable != null){
                            handleFailure(key, value, throwable);
                        }else {
                            handleSuccess(key, value, stringStringSendResult);
                        }
                    });
        }
    }

    private String buildKey(User user){
        return user.getFirstName() + "_" + user.getLastName() + ":" + user.getEmail();
    }

    private void handleSuccess(String key, String value, SendResult<String, String> sendResult){
        log.info("Message sent successfully for the key: {}, value : {}, and the partition is: {}", key, value, sendResult.getRecordMetadata().partition());
    }

    private void handleFailure(String key, String value, Throwable throwable){
        log.error("Error send the message is: {}", throwable.getMessage());
    }

    private ProducerRecord<String, String> buildProducerRecord(String key, String value, String topic){
        List<Header> recordHeaders = List.of(new RecordHeader(HEADER_EVENT_SOURCE, SOURCE_SCANNER.getBytes(StandardCharsets.UTF_8)));
        return new ProducerRecord<>(topic, null, key, value, recordHeaders);
    }

}
