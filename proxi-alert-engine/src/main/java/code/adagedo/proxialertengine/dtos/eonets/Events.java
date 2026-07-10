package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Events(

        @JsonProperty("id")
        String id,
        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("link")
        String link,
        @JsonProperty("categories")
        List<Category> categories,

        @JsonProperty("sources")
        List<Sources> sources,

        @JsonProperty("geometries")
        List<Geometry> geometries
) {
}
