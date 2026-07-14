package code.adagedo.proxialertengine.controller;

import code.adagedo.proxialertengine.dtos.OptInChannel;
import code.adagedo.proxialertengine.dtos.OptInStatus;
import code.adagedo.proxialertengine.dtos.request.OptInRequest;
import code.adagedo.proxialertengine.dtos.response.OptInData;
import code.adagedo.proxialertengine.dtos.response.OptInResponse;
import code.adagedo.proxialertengine.dtos.response.SubscriptionData;
import code.adagedo.proxialertengine.dtos.request.SubscriptionRequest;
import code.adagedo.proxialertengine.dtos.response.SubscriptionResponse;
import code.adagedo.proxialertengine.models.NotificationSetting;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@Slf4j
@RestController
@CrossOrigin(
        methods = {RequestMethod.POST, RequestMethod.GET}, origins = "*"
)
@RequiredArgsConstructor
public class SubController {

    private final UserService userService;

    @PostMapping("/api/v1/users/subscribe")
    public ResponseEntity<SubscriptionResponse> handleSubscription(@RequestBody SubscriptionRequest request){

        User user = userService.registerUser(request);

        SubscriptionData subscriptionData = new SubscriptionData(
                user.getEmail(),
                user.getPhoneNumber(),
                user.getLatitude(),
                user.getLongitude()
        );

        SubscriptionResponse response = new SubscriptionResponse(
                HttpStatusCode.valueOf(200),
                "Welcome to ProxySentry! You have successfully subscribed to real-time proximity disaster tracking. You will now receive immediate notifications for any environmental hazards detected near your location.",
                LocalDateTime.now(),
                subscriptionData
        );

        log.info("Registered new subscribed user {}", request.email());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/v1/users/subscribe/opt-in")
    public ResponseEntity<OptInResponse> handleOptIn(@RequestBody OptInRequest request){

        NotificationSetting notificationSetting = userService.handleSubscription(request, String.valueOf(OptInStatus.SUBSCRIBED));

        OptInData data = new OptInData(request.email(), String.valueOf(notificationSetting.getChannel()), OptInStatus.SUBSCRIBED);

        String bothChannelMessage = String.format(
                "Success! You have successfully subscribed to %s channel. We will alert you if an event occurs near your location.",
                request.channel()
        );

        String singleChannelMessage = String.format(
                "Success! You have successfully subscribed to %s channel. We will alert you if an event occurs near your location.",
                request.channel()
        );

        OptInResponse response = handleResponse(bothChannelMessage, singleChannelMessage, data, request);


        log.info("user updated his default subscription to {}", request.channel());
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/api/v1/users/subscribe/opt-out")
    public ResponseEntity<OptInResponse> handleOptOn(@RequestBody OptInRequest request){

        NotificationSetting notificationSetting = userService.handleSubscription(request, String.valueOf(OptInStatus.UNSUBSCRIBED));

        OptInData data = new OptInData(request.email(), String.valueOf(notificationSetting.getChannel()), OptInStatus.UNSUBSCRIBED);

        String singleChannelMessage = String.format(
                "Success! You have successfully unsubscribed to %s channel. We will alert you via %s if any event is detected near your location.",
                request.channel(), notificationSetting.getChannel()
        );

        String bothChannelMessage = String.format(
                "Success! You have successfully unsubscribed to %s channels you will not receive alert on events near your location.",
                request.channel()
        );
        OptInResponse response = handleResponse(bothChannelMessage, singleChannelMessage, data, request);

        log.info("user unscribed to {} channel", request.channel());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private OptInResponse handleResponse(String BothChannelMessage, String singleChannelMessage, OptInData data, OptInRequest request){

        OptInResponse response = null;

        if(request.channel().toUpperCase().equals(String.valueOf(OptInChannel.BOTH))){
            response = new OptInResponse(
                    HttpStatus.OK.value(),
                    BothChannelMessage,
                    LocalDateTime.now(),
                    data
            );
            return response;
        }else
            response = new OptInResponse(
                    HttpStatus.OK.value(),
                    singleChannelMessage,
                    LocalDateTime.now(),
                    data
            );
        return response;
    }
}
