package code.adagedo.proxialertengine.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;

public record SubscriptionRequest(
        String firstName,

        String lastName,

        @Email(message = "invalid email format")
        @NotBlank(message = "email is required")
        String email,

        @Pattern(regexp = "^\\+[1-9]\\d{1,14}$", message = "invalid phone number format use (e.g. +2348012345678)")
        @NotBlank(message = "phone number is required")
        String phoneNumber,

        BigDecimal latitude,

        BigDecimal longitude
) {
}
