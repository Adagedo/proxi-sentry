package code.adagedo.proxialertengine.dtos.eonets;

import java.util.List;

public record EonetPayload(
        String title,
        String description,
        String link,
        List<EonetCategory> categories
) {
}
