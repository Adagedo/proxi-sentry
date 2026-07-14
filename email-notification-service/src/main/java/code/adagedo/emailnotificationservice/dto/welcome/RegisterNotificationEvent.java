package code.adagedo.emailnotificationservice.dto.welcome;

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
