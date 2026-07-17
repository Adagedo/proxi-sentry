package code.adagedo.emailnotificationservice.service;


import code.adagedo.emailnotificationservice.dto.disaster_alert.NotificationEvent;
import code.adagedo.emailnotificationservice.dto.welcome.RegisterNotificationEvent;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Map;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectMapper objectMapper;

    private final JavaMailSender mailSender;

    public void processWelcomeNotification(ConsumerRecord<String, String> consumerRecord) throws IOException {
        try{
            RegisterNotificationEvent registerNotificationEvent = objectMapper.readValue(consumerRecord.value(), RegisterNotificationEvent.class);


        String event = registerNotificationEvent.eventType();
        String message = registerNotificationEvent.message();
        String subject = registerNotificationEvent.subject();
        String recipient_email = registerNotificationEvent.recipient().email();
        String recipient_name = registerNotificationEvent.recipient().name();


            ClassPathResource resource =
                    new ClassPathResource("templates/registration-notification.html");

            String html = new String(
                    resource.getInputStream().readAllBytes(),
                    StandardCharsets.UTF_8
            );

            Map<String, String> values = Map.of(
                    "event", event,
                    "recipientName", recipient_name,
                    "recipientEmail", recipient_email,
                    "message", message
            );

            for (Map.Entry<String, String> entry : values.entrySet()) {
                html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
            }

        sendMail(recipient_email, subject, html);
        log.info("sending welcome email to {}", recipient_email);
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

    public void processDisasterAlertNotification(ConsumerRecord<String, String> consumerRecord) throws IOException {

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


        ClassPathResource resource =
                new ClassPathResource("templates/emergency-alert.html");

        String html = new String(
                resource.getInputStream().readAllBytes(),
                StandardCharsets.UTF_8
        );

        Map<String, String> placeholders = Map.ofEntries(
                Map.entry("recipientName", recipientName),
                Map.entry("eventType", eventType),
                Map.entry("disasterType", disasterType),
                Map.entry("disasterName", disasterName),
                Map.entry("distanceInKm", String.valueOf(distanceInKm)),
                Map.entry("latitude", String.valueOf(latitude)),
                Map.entry("longitude", String.valueOf(longitude)),
                Map.entry("triggeredBy", triggeredBy),
                Map.entry("recipientEmail", recipientEmail),
                Map.entry("recipientPhoneNumber", recipientPhoneNumber),
                Map.entry("generatedAt", LocalDateTime.now().toString())
        );
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            html = html.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }

        sendMail(recipientEmail, eventType, html);
        log.info("sending disaster alert to user {}", recipientEmail);
    }

    @Async
    private void sendMail(String to, String subject, String body){
        try {

            MimeMessage mimeMessage = mailSender.createMimeMessage();

            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    false,
                    StandardCharsets.UTF_8.name()
            );

            helper.setTo(to);
            helper.setFrom("adagedosolomon52@gmail.com");
            helper.setSubject(subject);

            helper.setText(body, true);

            mailSender.send(mimeMessage);

        } catch (MessagingException | MailException exception) {
            log.warn("{}", exception.getMessage());
        }
    }
}
