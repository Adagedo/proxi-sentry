package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Sources(

        @JsonProperty("id")
        String id,

        @JsonProperty("uri")
        String uri
) {
}
