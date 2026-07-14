package code.adagedo.emailnotificationservice.dto.welcome;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.Instant;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RegisterNotificationEvent(
        @JsonProperty("messageId")
        String messageId,

        @JsonProperty("eventType")
        String eventType,

        @JsonProperty("recipient")
        Recipient recipient,

        @JsonProperty("subject")
        String subject,

        @JsonProperty("message")
        String message,

        @JsonProperty("createdAt")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSS'Z'", timezone = "UTC")
        Instant createdAt
) { }
