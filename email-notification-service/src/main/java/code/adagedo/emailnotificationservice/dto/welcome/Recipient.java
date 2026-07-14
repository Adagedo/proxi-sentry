package code.adagedo.emailnotificationservice.dto.welcome;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Recipient(
        @JsonProperty("email") String email,
        @JsonProperty("name") String name
) {
}