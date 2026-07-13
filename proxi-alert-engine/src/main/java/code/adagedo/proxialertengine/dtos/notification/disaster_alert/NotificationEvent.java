package code.adagedo.proxialertengine.dtos.notification.disaster_alert;

import java.time.Instant;

public record NotificationEvent(
        String eventId,
        String eventType,
        Instant timestamp,
        RecipientInfo recipient,
        AlertData payload
) {
}
