package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ProximityAlertService {

    private final UserRepository userRepository;
    private static final double EARTH_RADIUS_IN_KM = 6371.0;

    public List<User> processUsersToSendDisasterAlert(double disasterLat, double disasterLon, double radius){

        double degreeOffset = radius / 111.0;
        double minLat = disasterLat - degreeOffset;
        double maxLat = disasterLat + degreeOffset;
        double minLon = disasterLon - degreeOffset;
        double maxLon = disasterLon + degreeOffset;

        List<User> users = userRepository.findByLatitudeBetweenAndLongitudeBetween(minLat, maxLat, minLon, maxLon);

        List<User> usersToAlert = new ArrayList<>();

        for(User user: users){
            double distance = calculateDistance(disasterLat, disasterLon, user.getLatitude(), user.getLongitude());
            if(distance < radius){
                usersToAlert.add(user);
            }
        }

        return usersToAlert;
    }
    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS_IN_KM * c;
    }
}
