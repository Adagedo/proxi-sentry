package code.adagedo.proxialertengine.producer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class EventProducerTest {

    private KafkaTemplate<String, String> kafkaTemplate;

    @BeforeEach
    void setUp() {
    }

    @Test
    void publishEvents() {
    }
}