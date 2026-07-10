package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EonetCategory(

        @JsonProperty("id")
        String id,
        @JsonProperty("title")
        String title,

        @JsonProperty("link")
        String link,

        @JsonProperty("description")
        String description,

        @JsonProperty("layers")
        String layers
) {
}
