package code.adagedo.proxialertengine.dtos.notification.welcome_alert;

import java.time.Instant;
import java.util.UUID;

public record RegisterNotificationEvent(
        String messageId,
        String eventType,
        Recipient recipient,
        String subject,
        Instant createdAt
) {
}
