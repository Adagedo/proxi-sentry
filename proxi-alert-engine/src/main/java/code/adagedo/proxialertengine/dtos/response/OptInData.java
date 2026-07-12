package code.adagedo.proxialertengine.dtos.response;

import code.adagedo.proxialertengine.dtos.OptInStatus;

public record OptInData(
        String email,

        String channel,

        OptInStatus status
) {
}
