package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.dtos.OptInChannel;
import code.adagedo.proxialertengine.dtos.OptInStatus;
import code.adagedo.proxialertengine.dtos.eonets.EonetPayload;
import code.adagedo.proxialertengine.dtos.eonets.Events;
import code.adagedo.proxialertengine.dtos.eonets.Geometry;
import code.adagedo.proxialertengine.dtos.notification.disaster_alert.AlertData;
import code.adagedo.proxialertengine.dtos.notification.disaster_alert.NotificationEvent;
import code.adagedo.proxialertengine.dtos.notification.disaster_alert.RecipientInfo;
import code.adagedo.proxialertengine.models.NotificationSetting;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.producer.EventProducer;
import code.adagedo.proxialertengine.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;


@Slf4j
@Service
@RequiredArgsConstructor
public class DisasterEventService {


    private final ProximityAlertService proximityAlertService;
    private final StringRedisTemplate redisTemplate;
    private final NotificationRepository notificationRepository;
    private final EventProducer eventProducer;
    private static final String KNOWN_EVENTS_KEY = "proxy_sentry:known_events";

    @Value("${spring.kafka.topics.email_topic}")
    private String email_topic;

    @Value("${spring.kafka.topics.sms_topic}")
    private String sms_topic;

    @Transactional
    public void processAndSendDisasterAlertToKafkaTopic(EonetPayload eonetPayload){

        if(eonetPayload.events().isEmpty()){
            log.info("no events found for {}", LocalDateTime.now());
        }

        for (Events events: eonetPayload.events()) {

            if (events.geometries().isEmpty()) continue;

            Geometry latestDisasterLocation = events.geometries().getLast();
            String processedDisasterEvents = events.id()  + "_" + latestDisasterLocation.date();

            boolean isNewEvent = redisTemplate.opsForSet().add(KNOWN_EVENTS_KEY, processedDisasterEvents) == 1L;
            if (!isNewEvent){
                continue;
            }

            double longitude = latestDisasterLocation.coordinates().getFirst();
            double latitude = latestDisasterLocation.coordinates().get(1);
            double radius = getStandardRadiusInKm(events.categories().getFirst().title());

            List<User> users = proximityAlertService.processUsersToSendDisasterAlert(
                    BigDecimal.valueOf(latitude),
                    BigDecimal.valueOf(longitude),
                    radius
            );

            if(users.isEmpty()){
                log.info("Disaster alert skipped: No users found within {}km radius of coordinates ({}, {})", radius, latitude, longitude);
                return;
            }

            for (User user : users) {

                String name = user.getFirstName() + " " + user.getLastName();

                RecipientInfo recipientInfo = new RecipientInfo(
                        name,
                        user.getEmail(),
                        user.getPhoneNumber()
                        );

                AlertData alertData = new AlertData(
                        name,
                        events.categories().getFirst().title(),
                        events.title(),
                        radius,
                        latitude,
                        longitude
                );

                NotificationEvent event = new NotificationEvent(
                        String.valueOf(UUID.randomUUID()),
                        "PROXIMITY_ALERT",
                        Instant.now(),
                        recipientInfo,
                        alertData
                );

                NotificationSetting notificationSetting = notificationRepository.findByUser(user);

                switch (notificationSetting.getChannel()){
                    case OptInChannel.EMAIL -> {
                        switch (notificationSetting.getOptin_status()){
                            case OptInStatus.SUBSCRIBED -> {
                                eventProducer.publishEvents(event, email_topic, user);
                                log.info("Sending email alert for user {} to kafka topic", user.getEmail());
                            }
                            case OptInStatus.UNSUBSCRIBED -> log.info("Unsubscribed user {} not receiving email alert", user.getEmail());

                            default -> log.info("Not sending email alert to kafka topic due to invalid subtype... ");

                        }
                    }

                    case OptInChannel.SMS -> {
                        switch (notificationSetting.getOptin_status()){

                            case OptInStatus.SUBSCRIBED -> {
                                eventProducer.publishEvents(event, sms_topic, user);
                                log.info("Sending sms alter for user {} receiving sms alert", user.getEmail());
                            }

                            case OptInStatus.UNSUBSCRIBED -> log.info("Unsubscribed user {} for sms not receiving sms alert", user.getEmail());

                            default -> log.info("Not sending sms alert due to invalid subtype...");
                        }
                    }

                    case OptInChannel.BOTH -> {
                        switch (notificationSetting.getOptin_status()){

                            case OptInStatus.SUBSCRIBED -> {

                                eventProducer.publishEvents(event, sms_topic, user);
                                eventProducer.publishEvents(event, email_topic, user);

                                log.info("Sending email and sms alter to kafka topic for user {} ", user.getEmail());
                            }

                            case OptInStatus.UNSUBSCRIBED -> log.info("Unsubscribed user {} not receiving sms and email alert", user.getEmail());

                            default -> log.info("Not sending email and sms alert due to invalid subtype");

                        }
                    }
                }
            }
        }
    }

    private double getStandardRadiusInKm(String title){
        return switch (title){
            case "Drought" -> 500.0;
            case "Dust and Haze", "Snow" -> 150.0;
            case "Earthquakes", "Volcanoes" -> 100.0;
            case "Floods", "Manmade", "Water Color" -> 50.0;
            case "Landslides" -> 20.0;
            case "Sea and Lake Ice" -> 200.0;
            case "Severe Storms" -> 350.0;
            case "Temperature Extremes" -> 300.0;
            case "Wildfires" -> 40.0;
            default -> 50;
        };
    }

}
