package code.adagedo.proxialertengine.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(
        name = "notification_settings",
        indexes = {
                @Index(name = "idx_user_id", columnList = "user_id", unique = true)
        }
)
public class NotificationSetting extends BaseModel{


    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @EqualsAndHashCode.Exclude
    private User user;

    @Column
    private String channel;

    @Column
    private Boolean opt_in;
}
