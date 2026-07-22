package code.adagedo.smsnotificationservice.service;

import code.adagedo.smsnotificationservice.dto.disaster_alert.NotificationEvent;
import code.adagedo.smsnotificationservice.dto.request.SmsRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SmsServiceTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private RestClient restClient;

    @Mock
    private RestClient.Builder restClientBuilder;

    private SmsService smsService;

    private final String baseUrl = "https://api.sms-provider.com/send";
    private final String apiKey = "test-api-key";

    @BeforeEach
    void setUp() {
        when(restClientBuilder.build()).thenReturn(restClient);

        smsService = new SmsService(objectMapper, (RestClient) restClientBuilder);

        ReflectionTestUtils.setField(smsService, "api_key", apiKey);
        ReflectionTestUtils.setField(smsService, "base_url", baseUrl);
    }

    @Test
    @DisplayName("Should process disaster alert and send SMS successfully")
    void shouldProcessAndSendSmsAlert() throws JsonProcessingException {
        String payload = "{\"dummy\":\"alert\"}";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, "key", payload);

        NotificationEvent mockEvent = mock(NotificationEvent.class, RETURNS_DEEP_STUBS);
        when(mockEvent.payload().disasterName()).thenReturn("Earthquake");
        when(mockEvent.payload().disasterType()).thenReturn("SEISMIC");
        when(mockEvent.payload().distanceInKm()).thenReturn(12.5);
        when(mockEvent.payload().latitude()).thenReturn(34.0522);
        when(mockEvent.payload().longitude()).thenReturn(-118.2437);
        when(mockEvent.recipient().phoneNumber()).thenReturn("+1234567890");
        when(mockEvent.recipient().email()).thenReturn("user@example.com");

        when(objectMapper.readValue(payload, NotificationEvent.class)).thenReturn(mockEvent);

        RestClient.RequestBodyUriSpec uriSpec = mock(RestClient.RequestBodyUriSpec.class);
        RestClient.RequestBodySpec bodySpec = mock(RestClient.RequestBodySpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.post()).thenReturn(uriSpec);
        when(uriSpec.uri(baseUrl)).thenReturn(bodySpec);
        when(bodySpec.contentType(MediaType.APPLICATION_JSON)).thenReturn(bodySpec);
        when(bodySpec.body(any(SmsRequest.class))).thenReturn(bodySpec);
        when(bodySpec.header(anyString(), anyString())).thenReturn(bodySpec);
        when(bodySpec.retrieve()).thenReturn(responseSpec);

        ResponseEntity<Void> responseEntity = ResponseEntity.ok().build();
        when(responseSpec.toBodilessEntity()).thenReturn(responseEntity);

        smsService.processAndSendSmsAlert(record);

        verify(objectMapper, times(1)).readValue(payload, NotificationEvent.class);
        verify(restClient, times(1)).post();
        verify(bodySpec, times(1)).retrieve();
        verify(bodySpec).header("Authorization", "App " + apiKey);
    }

    @Test
    @DisplayName("Should throw exception when JSON parsing fails")
    void shouldThrowExceptionWhenParsingFails() throws JsonProcessingException {
        String payload = "invalid-json";
        ConsumerRecord<String, String> record = new ConsumerRecord<>("topic", 0, 0L, "key", payload);

        when(objectMapper.readValue(payload, NotificationEvent.class))
                .thenThrow(new RuntimeException("JSON Error"));

        assertThrows(RuntimeException.class, () -> smsService.processAndSendSmsAlert(record));
    }
}