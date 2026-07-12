package code.adagedo.proxialertengine.dtos.response;

import java.time.LocalDateTime;

public record OptInResponse(
        int status,
        String message,
        LocalDateTime timestamp,
        OptInData subscription
) {
}
