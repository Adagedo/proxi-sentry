package code.adagedo.proxialertengine.dtos.eonets;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;
import java.util.List;

public record Geometry(

        @JsonProperty("date")
        LocalDateTime date,

        @JsonProperty("type")
        String type,

        @JsonProperty("coordinates")
        List<Long> coordinates
){
}
