package code.adagedo.smsnotificationservice.service;

import code.adagedo.smsnotificationservice.dto.disaster_alert.NotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final ObjectMapper objectMapper;


    @PostConstruct
    public void intTwilio() {
        String accountSid = "";
        String authToken = "";
        Twilio.init(accountSid, authToken);
    }

    private void sendMsm(String toPhoneNumber, String messageBody){
       String twilioPhone = "";
        try{
            Message message = Message.creator(
                    new PhoneNumber(toPhoneNumber),
                    new PhoneNumber(twilioPhone),
                    messageBody
            ).create();
            log.info("Message sent successfully {}", message.getSid());
        }catch (Exception exception){
            log.warn("Failed to send SMS {}", exception.getMessage());
        }
    }

    public void processSmsAlert(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {

        NotificationEvent notificationEvent = objectMapper.readValue(consumerRecord.value(), NotificationEvent.class);

        String disasterName = notificationEvent.payload().disasterName();
        String disasterType = notificationEvent.payload().disasterType();
        var distanceInKm = notificationEvent.payload().distanceInKm();
        double latitude = notificationEvent.payload().latitude();
        double longitude = notificationEvent.payload().longitude();
        String recipientPhoneNumber = notificationEvent.recipient().phoneNumber();
        String email = notificationEvent.recipient().email();

        String smsMessage = String.format(
                "EMERGENCY ALERT FROM PROXI-SENTRY: %s (%s) reported %.2f km away from you. " +
                        "Location: %.4f, %.4f. Stay alert and take precautions.",
                disasterName,
                disasterType,
                distanceInKm,
                latitude,
                longitude
        );

        sendMsm(recipientPhoneNumber, smsMessage);
        log.info("sending sms to user with email {} and phone number {} ", email, recipientPhoneNumber);
    }

}
