package code.adagedo.smsnotificationservice.consumer;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SmsConsumer {

    
    public void SmsAlertNotification(ConsumerRecord<String, String> consumerRecord){

    }
}
