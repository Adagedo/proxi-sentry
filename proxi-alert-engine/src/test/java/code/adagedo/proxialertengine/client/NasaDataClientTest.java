package code.adagedo.proxialertengine.client;

import code.adagedo.proxialertengine.dtos.eonets.EonetPayload;
import code.adagedo.proxialertengine.exceptions.HttpClientConnectionException;
import code.adagedo.proxialertengine.service.DisasterEventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.RestClient;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NasaDataClientTest {

    @Mock
    private ObjectMapper mapper;

    @Mock
    private DisasterEventService disasterEventService;

    @Mock
    private RestClient restClient;
    private NasaDataClient nasaDataClient;

    @BeforeEach
    void setUp() {
        try (MockedStatic<RestClient> mockedRestClientClass = mockStatic(RestClient.class)) {
            mockedRestClientClass.when(RestClient::create).thenReturn(restClient);
            nasaDataClient = new NasaDataClient(mapper, disasterEventService);
        }
    }

    private RestClient.ResponseSpec stubRestClientChain() {

        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(restClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(anyString())).thenReturn(headersSpec);

        when(headersSpec.accept(MediaType.APPLICATION_JSON, MediaType.ALL)).thenReturn(headersSpec);

        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        return responseSpec;
    }

    @Test
    @DisplayName("Should successfully fetch, deserialize, and process disaster data on happy path")
    void fetchDisasterData_HappyPath_ShouldProcessData() throws IOException {

        String mockPayloadJson = "{\"events\":[]}";
        EonetPayload mockPayload = mock(EonetPayload.class);

        RestClient.ResponseSpec responseSpec = stubRestClientChain();
        when(responseSpec.body(String.class)).thenReturn(mockPayloadJson);

        when(mapper.readValue(mockPayloadJson, EonetPayload.class)).thenReturn(mockPayload);

        nasaDataClient.fetchDisasterData();

        verify(disasterEventService, times(1)).processAndSendDisasterAlertToKafkaTopic(mockPayload);
    }

    @Test
    @DisplayName("Should verify status checker registers and throws HttpClientConnectionException on 4xx status")
    @SuppressWarnings("unchecked")
    void fetchDisasterData_When4xxClientError_ShouldRegisterAndThrowException() throws IOException {

        RestClient.ResponseSpec responseSpec = stubRestClientChain();
        when(responseSpec.body(String.class)).thenReturn("{}");

        nasaDataClient.fetchDisasterData();

        ArgumentCaptor<Predicate<HttpStatusCode>> predicateCaptor = ArgumentCaptor.forClass(Predicate.class);
        ArgumentCaptor<RestClient.ResponseSpec.ErrorHandler> errorHandlerCaptor = ArgumentCaptor.forClass(RestClient.ResponseSpec.ErrorHandler.class);

        verify(responseSpec).onStatus(predicateCaptor.capture(), errorHandlerCaptor.capture());

        Predicate<HttpStatusCode> capturedPredicate = predicateCaptor.getValue();
        assertThat(capturedPredicate.test(HttpStatus.BAD_REQUEST)).isTrue();
        assertThat(capturedPredicate.test(HttpStatus.UNAUTHORIZED)).isTrue();
        assertThat(capturedPredicate.test(HttpStatus.INTERNAL_SERVER_ERROR)).isFalse();

        ClientHttpRequest mockRequest = mock(ClientHttpRequest.class);
        ClientHttpResponse mockResponse = mock(ClientHttpResponse.class);
        when(mockResponse.getStatusText()).thenReturn("API Rate Limit Exceeded");

        RestClient.ResponseSpec.ErrorHandler capturedErrorHandler = errorHandlerCaptor.getValue();
        assertThatThrownBy(() -> capturedErrorHandler.handle(mockRequest, mockResponse))
                .isInstanceOf(HttpClientConnectionException.class)
                .hasMessageContaining("API Rate Limit Exceeded");
    }

    @Test
    @DisplayName("Should propagate exception when json mapping fails")
    void fetchDisasterData_WhenMappingFails_ShouldPropagateException() {

        String invalidJson = "invalid-json";

        RestClient.ResponseSpec responseSpec = stubRestClientChain();
        when(responseSpec.body(String.class)).thenReturn(invalidJson);

        when(mapper.readValue(invalidJson, EonetPayload.class))
                .thenThrow(new RuntimeException("Malformed JSON structure"));

        assertThatThrownBy(() -> nasaDataClient.fetchDisasterData())
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Malformed JSON structure");

        verifyNoInteractions(disasterEventService);
    }
}