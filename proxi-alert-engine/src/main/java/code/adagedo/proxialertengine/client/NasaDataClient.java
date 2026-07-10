package code.adagedo.proxialertengine.client;

import code.adagedo.proxialertengine.dtos.eonets.EonetPayload;
import code.adagedo.proxialertengine.dtos.eonets.Events;
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

import java.net.NoRouteToHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class NasaDataClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper;
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
                            throw new NoRouteToHostException();
                        })
                )
                .body(String.class);
//        System.out.println(payload);
        EonetPayload eonetPayload = mapper.readValue(payload, EonetPayload.class);
        System.out.println(eonetPayload);
//        for (Events events: eonetPayload.events()){
//            System.out.println(events.geometries());
//            if (knownEventsId.contains(events.id())) {
//                log.info("Seen events: {}", events.id());
//            } else {
//                knownEventsId.add(events.id());
//            }
//        }
    }
}
