package code.adagedo.proxialertengine.service;

import code.adagedo.proxialertengine.dtos.OptInChannel;
import code.adagedo.proxialertengine.dtos.OptInStatus;
import code.adagedo.proxialertengine.dtos.notification.welcome_alert.Recipient;
import code.adagedo.proxialertengine.dtos.notification.welcome_alert.RegisterNotificationEvent;
import code.adagedo.proxialertengine.dtos.request.OptInRequest;
import code.adagedo.proxialertengine.dtos.request.SubscriptionRequest;
import code.adagedo.proxialertengine.exceptions.ChannelNameException;
import code.adagedo.proxialertengine.exceptions.UserAlreadySubscribedException;
import code.adagedo.proxialertengine.exceptions.UserNotSubscribedException;
import code.adagedo.proxialertengine.models.NotificationSetting;
import code.adagedo.proxialertengine.models.User;
import code.adagedo.proxialertengine.producer.EventProducer;
import code.adagedo.proxialertengine.repositories.NotificationRepository;
import code.adagedo.proxialertengine.repositories.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final NotificationRepository notificationRepository;

    private final EventProducer eventProducer;

    @Value("${spring.kafka.topics.user_registered_topic}")
    private String user_registered;

    @Transactional
    public User registerUser(SubscriptionRequest request){

        User existingUser = userRepository.findByEmail(request.email());

        if (existingUser!= null){
            log.info("Rejecting already subscribed user request");
            throw new UserAlreadySubscribedException("user already subscribed");
        }

        User new_user = User.builder()
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phoneNumber(request.phoneNumber())
                .email(request.email())
                .longitude(request.longitude())
                .latitude(request.latitude())
                .build();

        User user = userRepository.save(new_user);

        NotificationSetting notificationSetting = NotificationSetting.builder()
                        .user(user)
                                .channel(OptInChannel.BOTH)
                                        .optin_status(OptInStatus.SUBSCRIBED).build();

        notificationRepository.save(notificationSetting);
        log.info("saving new subscribed user to db");
        String name = user.getFirstName() + " " + user.getLastName();
        Recipient recipient = new Recipient(user.getEmail(), name);
        RegisterNotificationEvent event = new RegisterNotificationEvent(
                String.valueOf(UUID.randomUUID()),
                "USER REGISTERED",
                recipient,
                "Welcome to Proxy Sentry",
                Instant.now()
        );
        eventProducer.publishEvents(event, user_registered, user);
        return user;
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
