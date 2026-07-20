package code.adagedo.smsnotificationservice.service;

import code.adagedo.smsnotificationservice.dto.disaster_alert.NotificationEvent;
import code.adagedo.smsnotificationservice.dto.request.Content;
import code.adagedo.smsnotificationservice.dto.request.Destination;
import code.adagedo.smsnotificationservice.dto.request.SmsMessage;
import code.adagedo.smsnotificationservice.dto.request.SmsRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON;


@Slf4j
@Service
@RequiredArgsConstructor
public class SmsService {

    private final ObjectMapper objectMapper;

    @Value("${spring.sms.api_key}")
    private String api_key;

    @Value("${spring.sms.base_url}")
    private String base_url;

//    public void sendMsm() throws JsonProcessingException {
//
//        Content content = new Content("Welcome to proxy Sentry");
//
//        Destination destination = new Destination("+2348160990661".substring(1));
//
//        SmsMessage smsMessage = new SmsMessage(
//                List.of(destination),
//                "447491163443",
//                content
//        );
//
//        SmsRequest smsRequest = new SmsRequest(
//                List.of(smsMessage)
//        );
//
//        String json = objectMapper.writeValueAsString(smsRequest);
//
//        log.info("SMS JSON: {}", json);
//
////        String message = "{\"messages\":[{\"destinations\":[{\"to\":\"2348160990661\"}],\"sender\":\"447491163443\",\"content\":{\"text\":\"Congratulations on sending your first message. Go ahead and check the delivery report in the next step.\"}}]}";
//        RestClient restClient = RestClient.create();
//        ResponseEntity<Void> response = restClient.post()
//                .uri(base_url)
//                .contentType(APPLICATION_JSON)
//                .body(smsRequest)
//                .header("Authorization", "App " + api_key)
//                .header("Content-Type", "application/json")
//                .header("Accept", "application/json")
//                .retrieve()
//                .toBodilessEntity();
//
//        var status = response.getStatusCode().value();
//        log.info("Sending sms to users with email {}", status);
//    }

    public void processAndSendSmsAlert(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {

        try {
            NotificationEvent notificationEvent = objectMapper.readValue(consumerRecord.value(), NotificationEvent.class);

            String disasterName = notificationEvent.payload().disasterName();
            String disasterType = notificationEvent.payload().disasterType();
            var distanceInKm = notificationEvent.payload().distanceInKm();
            double latitude = notificationEvent.payload().latitude();
            double longitude = notificationEvent.payload().longitude();
            String recipientPhoneNumber = notificationEvent.recipient().phoneNumber();
            String recipientEmail = notificationEvent.recipient().email();

            String message = String.format(
                    """
                    URGENT SAFETY ALERT!
            
                    Disaster: %s
                    Type: %s
                    Distance: %s km
                    Location: %f, %f
                    Seek Shelter Immediately and monitor local News channels.
                    
                    Proxy Sentry Team.
                    """,
                    disasterName,
                    disasterType,
                    distanceInKm,
                    latitude,
                    longitude
            );

            Content content = new Content(message);

            Destination destination = new Destination(recipientPhoneNumber.substring(1));

            SmsMessage smsMessage = new SmsMessage(
                    List.of(destination),
                    "447491163443",
                    content
            );

            SmsRequest smsRequest = new SmsRequest(
                    List.of(smsMessage)
            );

            RestClient restClient = RestClient.create();
            ResponseEntity<Void> response = restClient.post()
                    .uri(base_url)
                    .contentType(APPLICATION_JSON)
                    .body(smsRequest)
                    .header("Authorization", "App " + api_key)
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .retrieve()
                    .toBodilessEntity();

            var status = response.getStatusCode().value();

            log.info(
                    "sending sms to {} with phone number {} sms status code {}",
                    recipientEmail,
                    recipientPhoneNumber,
                    status
            );

        }catch (Exception e){
            // kept the exception for debugging purpose
            log.error("--- TARGET EXCEPTION IDENTIFIED ---");
            log.error("Exception class: {}", e.getClass().getName());
            log.error("Error message details: {}", e.getMessage());
            log.error("Full stack trace stack dump: ", e);
            log.error("-----------------------------------");
            throw e;
        }
    }
}
