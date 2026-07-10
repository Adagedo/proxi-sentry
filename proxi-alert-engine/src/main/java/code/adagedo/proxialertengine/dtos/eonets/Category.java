package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Category(

        @JsonProperty("id")
        String id,
        @JsonProperty("title")
        String title
) {
}
