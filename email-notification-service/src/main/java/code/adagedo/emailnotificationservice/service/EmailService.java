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

import java.net.ConnectException;
import java.util.concurrent.CompletableFuture;


@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final ObjectMapper objectMapper;

    private final JavaMailSender mailSender;

    public void processWelcomeNotification(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        RegisterNotificationEvent registerNotificationEvent = objectMapper.readValue(consumerRecord.value(), RegisterNotificationEvent.class);

        // sending welcome email here
    }

    public void processDisasterAlertNotification(ConsumerRecord<String, String> consumerRecord) throws JsonProcessingException {
        NotificationEvent notificationEvent = objectMapper.readValue(consumerRecord.value(), NotificationEvent.class);


        // send alert email here
    }

    @Async
    public CompletableFuture<Void> sendMail(String to, String subject, String body){
        try{

            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setFrom("adagedosolomon52@gmail.com");
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);

            return CompletableFuture.completedFuture(null);
        }catch (RecoverableDataAccessException exception){
            log.warn("{}, ", exception.getMessage());
        }catch (MailSendException exception){
            log.warn("{} ", exception.getMessage());
        }
        return null;
    }
}
