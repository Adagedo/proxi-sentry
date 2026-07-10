package code.adagedo.proxialertengine.client;

import code.adagedo.proxialertengine.dtos.eonets.EonetPayload;
import code.adagedo.proxialertengine.dtos.eonets.Events;
import code.adagedo.proxialertengine.dtos.eonets.Geometry;
import code.adagedo.proxialertengine.exceptions.CustomHttpClientConnectionException;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.service.ProximityAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.data.crossstore.ChangeSetPersister;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.net.NoRouteToHostException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class NasaDataClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper;

    private final ProximityAlertService proximityAlertService;

    private final Set<String> knownEventsId = new HashSet<>();

    @EventListener(ApplicationReadyEvent.class)
//    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.HOURS) // commenting until full futures of the application is ready
    public void fetchDisasterData(){
        String uri = "https://eonet.gsfc.nasa.gov/api/v2.1/events?days=1";
        String payload = restClient.get()
                .uri(uri)
                .accept(MediaType.APPLICATION_JSON, MediaType.ALL)
                .retrieve()
                .onStatus(
                        HttpStatusCode::is4xxClientError, ((request, response) -> {
                            throw new CustomHttpClientConnectionException(response.getStatusText());
                        })
                )
                .body(String.class);

//        System.out.println(payload);
        EonetPayload eonetPayload = mapper.readValue(payload, EonetPayload.class);
//        System.out.println(eonetPayload);
        for (Events events: eonetPayload.events()){
//            System.out.println(events.geometries());
            if (knownEventsId.contains(events.id())) {
                log.info("Seen events: {}", events.id());
            } else {
                knownEventsId.add(events.id());
            }
            for (Geometry geometry: events.geometries()){
               double longitude = geometry.coordinates().getFirst();
                double latitude = geometry.coordinates().get(1);
                double radius = getStandardRadiusInKm(events.title());
                List<User> users = proximityAlertService.processUsersToSendDisasterAlert(longitude, latitude, radius);
                for (User user: users){
                    String userEmail = user.getEmail();
                    String userPhoneNumber = user.getPhoneNumber();
                    // send publish
                }
//                System.out.println(co);
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
