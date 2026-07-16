package code.adagedo.proxialertengine.producer;

import code.adagedo.proxialertengine.models.User;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.header.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventProducerTest {

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private User user;

    @InjectMocks
    private EventProducer eventProducer;

    @Captor
    private ArgumentCaptor<ProducerRecord<String, String>> producerRecordArgumentCaptor;

    private static final String TOPIC = "test-topic";
    private static final String FIRST_NAME = "John";
    private static final String LAST_NAME = "Doe";
    private static final String EMAIL = "john.doe@example.com";
    private static final String EXPECTED_KEY = "John_Doe:john.doe@example.com";

    record TestRecord(String id, String message){}

    static class TestClass{}

    @BeforeEach
    void setUp() {
    }

    @Test
    @DisplayName("should publish event successfully when input is a record")
    void publishEvents_WhenEventIsRecord_ShouldSendSuccessfully() {

        TestRecord event = new TestRecord("1234", "Test message");

        String jsonPayLoad = "{\"id\":\"123\",\"message\":\"Test message\"}";

        when(user.getFirstName()).thenReturn(FIRST_NAME);
        when(user.getLastName()).thenReturn(LAST_NAME);
        when(user.getEmail()).thenReturn(EMAIL);
        when(objectMapper.writeValueAsString(event)).thenReturn(jsonPayLoad);

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        RecordMetadata metadata = new RecordMetadata(new TopicPartition(TOPIC, 1), 0L, 0, 0L, 0, 0);
        SendResult<String, String> sendResult = new SendResult<>(null, metadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        eventProducer.publishEvents(event, TOPIC, user);

        verify(kafkaTemplate).send(producerRecordArgumentCaptor.capture());

        ProducerRecord<String, String> captureRecord = producerRecordArgumentCaptor.getValue();

        assertEquals(TOPIC, captureRecord.topic());
        assertEquals(EXPECTED_KEY, captureRecord.key());
        assertEquals(jsonPayLoad, captureRecord.value());

        Header sourceHeader = captureRecord.headers().lastHeader(EventProducer.getHEADER_EVENT_SOURCE());
        assertNotNull(sourceHeader);
        assertEquals(EventProducer.getSOURCE_SCANNER(), new String(sourceHeader.value(), StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName("Should handle Kafka send failure successfully")
    void publishEvents_WhenKafkaFails_ShouldHandleFailure(){
        TestRecord event = new TestRecord("123", "Test Message");
        String jsonPayload = "{\"id\":\"123\",\"message\":\"Test Message\"}";

        when(user.getFirstName()).thenReturn(FIRST_NAME);
        when(user.getLastName()).thenReturn(LAST_NAME);
        when(user.getEmail()).thenReturn(EMAIL);
        when(objectMapper.writeValueAsString(event)).thenReturn(jsonPayload);

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka timeout exception"));

        when(kafkaTemplate.send(any(ProducerRecord.class))).thenReturn(future);

        eventProducer.publishEvents(event, TOPIC, user);

        verify(kafkaTemplate).send(any(ProducerRecord.class));
    }

    @Test
    @DisplayName("Should not publish event when the input is not a record")
    void publishEvents_WhenEventIsNotRecord_ShouldDoNothing(){

        TestClass testClass = new TestClass();

        eventProducer.publishEvents(testClass, TOPIC, user);

        verifyNoInteractions(objectMapper);
        verifyNoInteractions(kafkaTemplate);
    }
}