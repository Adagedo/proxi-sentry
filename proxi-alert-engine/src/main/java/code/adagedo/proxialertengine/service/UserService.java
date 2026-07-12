package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.dtos.SubscriptionRequest;
import code.adagedo.proxialertengine.exceptions.UserAlreadySubscribedException;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    public User registerUser(SubscriptionRequest request){
        User user1 = userRepository.findByEmail(request.email());
        if (user1!= null){
            log.info("Rejecting already subscribed user request");
            throw new UserAlreadySubscribedException("user already subscribed");
        }

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .longitude(request.longitude())
                .latitude(request.latitude())
                .build();
        log.info("saving new subscribed user to db");
        return userRepository.save(user);
    }
}
