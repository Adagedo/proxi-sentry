package code.adagedo.proxialertengine.dtos.response;

import java.math.BigDecimal;

public record SubscriptionData(
        String email,
        String phoneNumber,
        BigDecimal latitude,
        BigDecimal longitude

) {
}
