package code.adagedo.proxialertengine.dtos.notification.welcome_alert;

public record Metadata(
        String source,
        String correlationId,
        String traceId,
        String requestedBy
) {
}
