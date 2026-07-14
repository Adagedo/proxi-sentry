package code.adagedo.emailnotificationservice.dto.welcome;

public record Metadata(
        String source,
        String correlationId,
        String traceId,
        String requestedBy
) {
}
