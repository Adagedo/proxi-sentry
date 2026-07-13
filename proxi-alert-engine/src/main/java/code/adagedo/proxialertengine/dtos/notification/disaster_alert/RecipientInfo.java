package code.adagedo.proxialertengine.dtos.notification.disaster_alert;

import code.adagedo.proxialertengine.dtos.OptInChannel;

public record RecipientInfo (

        String userName,
        String email,
        String phoneNumber
){
}
