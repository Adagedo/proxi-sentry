package code.adagedo.proxialertengine.dtos.eonets;

import java.time.LocalDateTime;
import java.util.List;

public record Geometry(
        LocalDateTime date,
        String type,
        List<Long> coordinates
){
}
