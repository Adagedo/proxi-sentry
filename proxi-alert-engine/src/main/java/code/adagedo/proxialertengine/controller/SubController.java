package code.adagedo.proxialertengine.controller;

import code.adagedo.proxialertengine.dtos.SubscriptionData;
import code.adagedo.proxialertengine.dtos.SubscriptionRequest;
import code.adagedo.proxialertengine.dtos.SubscriptionResponse;
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
        methods = {RequestMethod.POST, RequestMethod.GET}
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
}
