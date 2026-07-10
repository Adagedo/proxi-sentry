package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.dtos.SubscriptionRequest;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    public User registerUser(SubscriptionRequest request){

        User user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .longitude(request.longitude())
                .latitude(request.latitude())
                .build();

        return userRepository.save(user);
    }
}
