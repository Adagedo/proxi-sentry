package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Sources(

        @JsonProperty("id")
        String id,

        @JsonProperty("url")
        String url
) {
}
