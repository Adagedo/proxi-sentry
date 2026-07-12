package code.adagedo.proxialertengine.dtos.response;

import org.springframework.http.HttpStatusCode;

import java.time.LocalDateTime;

public record SubscriptionResponse(
        HttpStatusCode status,
        String message,
        LocalDateTime timestamp,
        SubscriptionData subscriptionData
) {
}
