package code.adagedo.proxialertengine.repositories;

import code.adagedo.proxialertengine.models.NotificationSetting;
import code.adagedo.proxialertengine.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationSetting, Long> {
    NotificationSetting findByUser(User user);
}
