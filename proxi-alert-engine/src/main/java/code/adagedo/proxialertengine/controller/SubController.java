package code.adagedo.proxialertengine.controller;

import code.adagedo.proxialertengine.dtos.SubscriptionData;
import code.adagedo.proxialertengine.dtos.SubscriptionRequest;
import code.adagedo.proxialertengine.dtos.SubscriptionResponse;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@CrossOrigin(
        methods = {RequestMethod.POST, RequestMethod.GET}
)
public class SubController {

    private UserService userService;

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
                "Welcome to Proxi-Sentry! You have successfully subscribed to real-time proximity disaster tracking.",
                LocalDateTime.now(),
                subscriptionData
        );
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}
