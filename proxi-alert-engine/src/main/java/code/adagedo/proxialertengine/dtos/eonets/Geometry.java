package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record Geometry(

        @JsonProperty("date")
        String date,

        @JsonProperty("type")
        String type,

        @JsonProperty("coordinates")
        List<Double> coordinates
){
}
