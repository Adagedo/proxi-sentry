package code.adagedo.proxialertengine.client;

import code.adagedo.proxialertengine.dtos.eonets.EonetPayload;
import code.adagedo.proxialertengine.exceptions.HttpClientConnectionException;
import code.adagedo.proxialertengine.service.DisasterEventService;

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

import java.util.*;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class NasaDataClient {

    private final RestClient restClient = RestClient.create();
    private final ObjectMapper mapper;

    private final DisasterEventService disasterEventService;


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
        disasterEventService.processAndSendDisasterAlertToKafkaTopic(eonetPayload);
    }

}
