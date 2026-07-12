package code.adagedo.proxialertengine.dtos.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OptInRequest(
        @Email(message = "invalid email format")
        @NotBlank(message = "email is required")
        String email,

        @NotBlank(message = "channel is required")
        String channel // Email, SMS, Both
) {
}
