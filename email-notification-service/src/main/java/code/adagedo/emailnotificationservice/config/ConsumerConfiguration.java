package code.adagedo.emailnotificationservice.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.TopicPartition;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.kafka.autoconfigure.ConcurrentKafkaListenerContainerFactoryConfigurer;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.ContainerCustomizer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.ExponentialBackOffWithMaxRetries;
import org.springframework.mail.MailSendException;

import java.util.List;

@Configuration
@EnableKafka
@Slf4j
@RequiredArgsConstructor
public class ConsumerConfiguration {

    @Value("${spring.kafka.topics.retry}")
    private String retryTopic;

    @Value("${spring.kafka.topics.dlt}")
    private String deadLetterTopic;

    private final KafkaProperties properties;

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Bean
    public DeadLetterPublishingRecoverer publishingRecoverer() {
        return new DeadLetterPublishingRecoverer(kafkaTemplate, (r, e) -> {
            if (e.getCause() instanceof RecoverableDataAccessException || e.getCause() instanceof MailSendException) {
                log.info("Exception found. Routing record to retry topic: {}", retryTopic);
                return new TopicPartition(retryTopic, r.partition());
            } else {
                log.info("Non-recoverable exception found. Routing record to DLT: {}", deadLetterTopic);
                return new TopicPartition(deadLetterTopic, r.partition());
            }
        });
    }

    @Bean
    public DefaultErrorHandler defaultErrorHandler(ConsumerRecordRecoverer recordRecoverer){

        var exceptionsToIgnoreList = List.of(
                IllegalArgumentException.class
        );
        var errorHandler = buildErrorHandler(recordRecoverer);
        exceptionsToIgnoreList.forEach(errorHandler::addNotRetryableExceptions);

        errorHandler.setRetryListeners(((record, ex, deliveryAttempt) -> {
            assert ex != null;
            log.warn("Record processing failed. Current delivery attempt: {}. Error: {}", deliveryAttempt, ex.getMessage());
        }));
        return errorHandler;
    }

    private @NonNull DefaultErrorHandler buildErrorHandler(ConsumerRecordRecoverer recoverer) {
        // Exponential Backoff Strategy
        var exponentialBackOff = new ExponentialBackOffWithMaxRetries(2);
        exponentialBackOff.setInitialInterval(1000L);
        exponentialBackOff.setMultiplier(2.0);
        exponentialBackOff.setMaxInterval(2000L);

        return new DefaultErrorHandler(recoverer, exponentialBackOff);
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<?, ?> kafkaListenerContainerFactory(
            ConcurrentKafkaListenerContainerFactoryConfigurer configurer,
            ObjectProvider<ConsumerFactory<Object, Object>> kafkaConsumerFactory,
            ObjectProvider<ContainerCustomizer<Object, Object, ConcurrentMessageListenerContainer<Object, Object>>> kafkaContainerCustomizer,
            DefaultErrorHandler defaultErrorHandler
    ) {
        ConcurrentKafkaListenerContainerFactory<Object, Object> factory = new ConcurrentKafkaListenerContainerFactory<>();
        configurer.configure(factory, kafkaConsumerFactory
                .getIfAvailable(() -> new DefaultKafkaConsumerFactory<>(this.properties.buildConsumerProperties())));

        kafkaContainerCustomizer.ifAvailable(factory::setContainerCustomizer);
        factory.setConcurrency(3);

        factory.setCommonErrorHandler(defaultErrorHandler);

        return factory;
    }
}
