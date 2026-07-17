package code.adagedo.smsnotificationservice.dto.disaster_alert;

public record AlertData(
        String userName,
        String disasterType,
        String disasterName,
        double distanceInKm,
        double latitude,
        double longitude
) {
}
