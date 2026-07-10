package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record EonetPayload(

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("link")
        String link,

        @JsonProperty("events")
        List<Events> events
) {
}
