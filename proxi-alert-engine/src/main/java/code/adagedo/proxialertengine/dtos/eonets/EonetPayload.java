package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record EonetPayload(

        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("link")
        String link,

        @JsonProperty("categories")
        List<EonetCategory> categories
) {
}
