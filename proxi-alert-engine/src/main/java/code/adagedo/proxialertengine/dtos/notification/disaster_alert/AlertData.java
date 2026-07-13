package code.adagedo.proxialertengine.dtos.notification.disaster_alert;

public record AlertData(
        String userName,
        String disasterType,
        String disasterName,
        double distanceInKm,
        double latitude,
        double longitude
) {
}
