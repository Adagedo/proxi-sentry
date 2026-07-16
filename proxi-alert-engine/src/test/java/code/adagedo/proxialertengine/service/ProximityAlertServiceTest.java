package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.within;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;


class ProximityAlertServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProximityAlertService proximityAlertService;

    @BeforeEach
    void setUp() {

        MockitoAnnotations.openMocks(this);
    }
    private static final BigDecimal DISASTER_LAT = BigDecimal.valueOf(40.7128);
    private static final BigDecimal DISASTER_LON = BigDecimal.valueOf(-74.0060);
    private static final double RADIUS_KM = 10.0;

    @Test
    @DisplayName("Should return empty list immediately when repository returns no users in bounding box")
    public void processUsersToSendDisasterAlert_WhenNoUsersInBoundingBox_ShouldReturnEmptyList() {

        when(userRepository.findByLatitudeBetweenAndLongitudeBetween(any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<User> result = proximityAlertService.processUsersToSendDisasterAlert(DISASTER_LAT, DISASTER_LON, RADIUS_KM);

        assertThat(result).isEmpty();
        verify(userRepository, times(1)).findByLatitudeBetweenAndLongitudeBetween(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Should correctly filter users: include inside radius, exclude diagonal outside radius")
    public void processUsersToSendDisasterAlert_ShouldCorrectlyIncludeAndExcludeUsers(){

        User userOne = User.builder()
                .firstName("Inside")
                .lastName("User")
                .latitude(BigDecimal.valueOf(40.7200))
                .longitude(BigDecimal.valueOf(-74.0000))
                .build();

        User userTwo = User.builder()
                .firstName("Outside")
                .lastName("User")
                .latitude(BigDecimal.valueOf(40.8000))
                .longitude(BigDecimal.valueOf(-73.9160))
                .build();

        when(userRepository.findByLatitudeBetweenAndLongitudeBetween(any(), any(), any(), any()))
                .thenReturn(List.of(userTwo, userOne));

        List<User> result = proximityAlertService.processUsersToSendDisasterAlert(DISASTER_LAT, DISASTER_LON, RADIUS_KM);

        assertThat(result)
                .hasSize(1)
                .containsExactly(userOne)
                .doesNotContain(userTwo);

        ArgumentCaptor<BigDecimal> minLatCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> maxLatCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> minLonCaptor = ArgumentCaptor.forClass(BigDecimal.class);
        ArgumentCaptor<BigDecimal> maxLonCaptor = ArgumentCaptor.forClass(BigDecimal.class);

        verify(userRepository).findByLatitudeBetweenAndLongitudeBetween(
                minLatCaptor.capture(),
                maxLatCaptor.capture(),
                minLonCaptor.capture(),
                maxLonCaptor.capture()
        );

        double expectedOffset = RADIUS_KM / 111.0;
        double expectedMinLat = DISASTER_LAT.doubleValue() - expectedOffset;
        double expectedMaxLat = DISASTER_LAT.doubleValue() + expectedOffset;
        double expectedMinLon = DISASTER_LON.doubleValue() - expectedOffset;
        double expectedMaxLon = DISASTER_LON.doubleValue() + expectedOffset;

        assertThat(minLatCaptor.getValue().doubleValue()).isCloseTo(expectedMinLat, within(1e-9));
        assertThat(maxLatCaptor.getValue().doubleValue()).isCloseTo(expectedMaxLat, within(1e-9));
        assertThat(minLonCaptor.getValue().doubleValue()).isCloseTo(expectedMinLon, within(1e-9));
        assertThat(maxLonCaptor.getValue().doubleValue()).isCloseTo(expectedMaxLon, within(1e-9));
    }

    @Test
    @DisplayName("Should exclude users positioned exactly on or beyond the exact radius boundary limit")
    public void processUsersToSendDisasterAlert_WhenUserIsOnExactBoundary_ShouldExcludeUser(){
        User boundaryUser = User.builder()
                .firstName("Boundary")
                .lastName("User")
                .latitude(BigDecimal.valueOf(40.802732161))
                .longitude(BigDecimal.valueOf(-74.0060))
                .build();

        when(userRepository.findByLatitudeBetweenAndLongitudeBetween(any(), any(), any(), any()))
                .thenReturn(List.of(boundaryUser));

        List<User> result = proximityAlertService.processUsersToSendDisasterAlert(DISASTER_LAT, DISASTER_LON, RADIUS_KM);

        assertThat(result).isEmpty();
    }
}