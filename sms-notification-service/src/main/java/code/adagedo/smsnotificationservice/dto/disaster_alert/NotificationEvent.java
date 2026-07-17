package code.adagedo.smsnotificationservice.dto.disaster_alert;


import java.time.Instant;

public record NotificationEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        RecipientInfo recipient,
        AlertData payload
) {
}
