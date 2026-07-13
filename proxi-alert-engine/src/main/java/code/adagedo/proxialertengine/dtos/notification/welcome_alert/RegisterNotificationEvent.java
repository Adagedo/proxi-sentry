package code.adagedo.proxialertengine.dtos.notification.welcome_alert;

import java.time.Instant;

public record RegisterNotificationEvent(
        String messageId,
        String eventType,
        Recipient recipient,
        String subject,
        String message,
        Instant createdAt
) {
}
