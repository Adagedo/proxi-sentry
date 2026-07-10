package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record Events(

        @JsonProperty("id")
        String id,
        @JsonProperty("title")
        String title,

        @JsonProperty("description")
        String description,

        @JsonProperty("categories")
        List<Category> categories,

        @JsonProperty("geometries")
        List<Geometry> geometries,
        @JsonProperty("source")
        List<Sources> sources
) {
}
