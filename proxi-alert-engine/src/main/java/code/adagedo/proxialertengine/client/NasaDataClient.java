package code.adagedo.proxialertengine.client;

import code.adagedo.proxialertengine.dtos.eonets.EonetPayload;
import code.adagedo.proxialertengine.dtos.eonets.Events;
import code.adagedo.proxialertengine.dtos.eonets.Geometry;
import code.adagedo.proxialertengine.exceptions.HttpClientConnectionException;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.service.ProximityAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class NasaDataClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper;

    private final ProximityAlertService proximityAlertService;

    private final Set<String> knownEvents = new HashSet<>();

    @EventListener(ApplicationReadyEvent.class)
    @Scheduled(fixedRate = 10, timeUnit = TimeUnit.SECONDS) // commenting until full futures of the application is ready
    public void fetchDisasterData(){
        String uri = "https://eonet.gsfc.nasa.gov/api/v2.1/events?days=1";
        String payload = restClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON, MediaType.ALL)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError, ((request, response) -> {
                            throw new HttpClientConnectionException(response.getStatusText());
                        })
                )
                .body(String.class);

        EonetPayload eonetPayload = mapper.readValue(payload, EonetPayload.class);

        for (Events events: eonetPayload.events()) {
            System.out.println(events.categories().getFirst().title());

            if (events.geometries().isEmpty()) continue;

            Geometry latestDisasterLocation = events.geometries().getLast();
            String processedDisasterEvents = events.id()  + "_" + latestDisasterLocation.date();

            if(knownEvents.contains(processedDisasterEvents)){
                continue;
            }
            knownEvents.add(processedDisasterEvents);

            double longitude = latestDisasterLocation.coordinates().getFirst();
            double latitude = latestDisasterLocation.coordinates().get(1);
            double radius = getStandardRadiusInKm(events.categories().getFirst().title());

            List<User> users = proximityAlertService.processUsersToSendDisasterAlert(
                    BigDecimal.valueOf(latitude),
                    BigDecimal.valueOf(longitude),
                    radius
            );
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
