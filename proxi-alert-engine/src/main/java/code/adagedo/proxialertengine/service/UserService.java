package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.dtos.OptInChannel;
import code.adagedo.proxialertengine.dtos.OptInStatus;
import code.adagedo.proxialertengine.dtos.request.OptInRequest;
import code.adagedo.proxialertengine.dtos.request.SubscriptionRequest;
import code.adagedo.proxialertengine.exceptions.ChannelNameException;
import code.adagedo.proxialertengine.exceptions.UserAlreadySubscribedException;
import code.adagedo.proxialertengine.exceptions.UserNotSubscribedException;
import code.adagedo.proxialertengine.models.NotificationSetting;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.repositories.NotificationRepository;
import code.adagedo.proxialertengine.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final NotificationRepository notificationRepository;

    @Transactional
    public User registerUser(SubscriptionRequest request){

        User existingUser = userRepository.findByEmail(request.email());

        if (existingUser!= null){
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

        NotificationSetting notificationSetting = NotificationSetting.builder()
                        .user(user)
                                .channel(OptInChannel.BOTH)
                                        .optin_status(OptInStatus.SUBSCRIBED).build();

        log.info("saving new subscribed user to db");

        notificationRepository.save(notificationSetting);

        return userRepository.save(user);
    }

    @Transactional
    public NotificationSetting handleSubscription(OptInRequest request, String status){

        User user = userRepository.findByEmail(request.email());

        if (user== null){
            log.info("Rejecting OptIn request for unsubscribed users...");
            throw new UserNotSubscribedException("Please subscribe to perform this action");
        }

        if(!OptInChannel.isValid(request.channel())){
            log.info("Rejecting request for invalid channel for unsubscribed users...");
            throw new ChannelNameException("invalid channel name");
        }

        NotificationSetting existingSettings = notificationRepository.findByUser(user);

        if (existingSettings == null){

            NotificationSetting notificationSetting = NotificationSetting.builder()
                    .channel(OptInChannel.valueOf(request.channel().toUpperCase()))
                        .user(user)
                            .optin_status(OptInStatus.valueOf(status))
                                .build();

            if(OptInStatus.valueOf(status).equals(OptInStatus.SUBSCRIBED)){
                log.info("saving new optIn notification settings for user {} to db", request.email());
            }

            log.info("saving new optOut notification settings for user {} to db", request.email());

            return notificationRepository.save(notificationSetting);

        }else {

            existingSettings.setChannel(OptInChannel.valueOf(request.channel().toUpperCase()));
            existingSettings.setOptin_status(OptInStatus.valueOf(status.toUpperCase()));

            if(OptInStatus.valueOf(status).equals(OptInStatus.SUBSCRIBED)){
                log.info("saving existing optIn notification settings for user {} to db", request.email());
            }

            log.info("saving existing optOut notification settings for user {} to db", request.email());

            return notificationRepository.save(existingSettings);
        }
    }
}
