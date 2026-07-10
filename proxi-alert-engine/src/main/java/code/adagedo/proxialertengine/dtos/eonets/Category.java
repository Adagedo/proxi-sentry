package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Category(

        @JsonProperty("id")
        Integer id,
        @JsonProperty("title")
        String title
) {
}
