package code.adagedo.emailnotificationservice.service;


import code.adagedo.emailnotificationservice.dto.disaster_alert.NotificationEvent;
import code.adagedo.emailnotificationservice.dto.welcome.RegisterNotificationEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.dao.RecoverableDataAccessException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectMapper objectMapper;

    private final JavaMailSender mailSender;

    public void processWelcomeNotification(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        try{
            RegisterNotificationEvent registerNotificationEvent = objectMapper.readValue(consumerRecord.value(), RegisterNotificationEvent.class);


        String event = registerNotificationEvent.eventType();
        String message = registerNotificationEvent.message();
        String subject = registerNotificationEvent.subject();
        String recipient_email = registerNotificationEvent.recipient().email();
        String recipient_name = registerNotificationEvent.recipient().name();

        String body = """
            Hello %s,
       \s
            Welcome to ProxySentry!\s
       \s
            We are sending you this update regarding your proxisentry registration.
       \s
            Message Details:
            --------------------------------------------------
            %s
            --------------------------------------------------
       \s
            Registered Account Details:
             - Name: %s
             - Email: %s
       \s
            If you have any questions, feel free to reply directly to this email.
       \s
            Best regards,
            The ProxySentry Team
       \s""".formatted(
                recipient_name,
                message,
                recipient_name,
                recipient_email
        );

        // sending welcome email here
        sendMail(recipient_email, subject, body);
        log.info("sending welcome email to {}", recipient_email);
        }catch (Exception e){
            log.error("--- TARGET EXCEPTION IDENTIFIED ---");
            log.error("Exception class: {}", e.getClass().getName());
            log.error("Error message details: {}", e.getMessage());
            log.error("Full stack trace stack dump: ", e);
            log.error("-----------------------------------");
            throw e;
        }
    }

    public void processDisasterAlertNotification(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {

        NotificationEvent notificationEvent = objectMapper.readValue(consumerRecord.value(), NotificationEvent.class);

        String eventType = notificationEvent.eventType();
        String disasterName = notificationEvent.payload().disasterName();
        String disasterType = notificationEvent.payload().disasterType();
        var distanceInKm = notificationEvent.payload().distanceInKm();
        String triggeredBy = notificationEvent.payload().userName();
        double latitude = notificationEvent.payload().latitude();
        double longitude = notificationEvent.payload().longitude();
        String recipientEmail = notificationEvent.recipient().email();
        String recipientName = notificationEvent.recipient().userName();
        String recipientPhoneNumber = notificationEvent.recipient().phoneNumber();

        String message = String.format(
                """
                URGENT SAFETY ALERT: %1$s [%3$s - %2$s]
                
                Hello %9$s,
                
                This is an automated emergency notification from ProxySentry. A critical environmental event has been flagged near your monitored parameter.
                
                Disaster Threat Assessment:
                --------------------------------------------------
                Event Action:  %1$s
                Disaster Type: %3$s
                Disaster Name: %2$s
                Proximity:     %4$s km away from your location
                
                Geographic Anchors:
                - Latitude:  %6$s
                - Longitude: %7$s
                --------------------------------------------------
                
                Please take immediate precautions, monitor local news channels, and follow instructions from safety officials in your vicinity.
                
                System Dispatch Logs:
                - Event Triggered By: %5$s
                - Notification Target: %8$s
                - SMS Backup Destination: %10$s
                
                Stay safe,
                The ProxySentry Alert System Team
                """,
                eventType,
                disasterName,
                disasterType,
                distanceInKm,
                triggeredBy,
                latitude,
                longitude,
                recipientEmail,
                recipientName,
                recipientPhoneNumber
        );

        sendMail(recipientEmail, eventType, message);
        log.info("sending disaster alert to user {}", recipientEmail);
    }

    @Async
    private void sendMail(String to, String subject, String body){

        try{

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("adagedosolomon52@gmail.com");
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

        }catch (RecoverableDataAccessException exception){
            log.warn("{}, ", exception.getMessage());
        }catch (MailSendException exception){
            log.warn("{} ", exception.getMessage());
        }
    }
}
