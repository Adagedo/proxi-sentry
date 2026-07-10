package code.adagedo.proxialertengine.dtos.eonets;

import java.util.List;

public record Events(
        String id,
        String title,
        String description,
        List<Category> categories,
        List<Geometry> geometries,
        List<Sources> sources
) {
}
