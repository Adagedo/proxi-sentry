package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.dtos.eonets.EonetPayload;
import code.adagedo.proxialertengine.dtos.eonets.Events;
import code.adagedo.proxialertengine.dtos.eonets.Geometry;
import code.adagedo.proxialertengine.models.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Slf4j
@Service
@RequiredArgsConstructor
public class DisasterEventService {


    private final ProximityAlertService proximityAlertService;
    private final StringRedisTemplate redisTemplate;
    private static final String KNOWN_EVENTS_KEY = "proxy_sentry:known_events";

    public void processAndSendDisasterAlertToKafkaTopic(EonetPayload eonetPayload){

        if(eonetPayload.events().isEmpty()){
            log.info("not events found for {}", LocalDateTime.now());
        }

        for (Events events: eonetPayload.events()) {
            System.out.println(events.categories().getFirst().title());

            if (events.geometries().isEmpty()) continue;

            Geometry latestDisasterLocation = events.geometries().getLast();
            System.out.println(latestDisasterLocation);
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

            log.info("Publishing {} disaster alert events to Kafka...", users.size());

            for (User user : users) {
                String userEmail = user.getEmail();
                System.out.println(userEmail);
                // send email to found users
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
