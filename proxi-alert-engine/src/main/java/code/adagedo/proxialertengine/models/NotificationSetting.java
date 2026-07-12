package code.adagedo.proxialertengine.models;

import code.adagedo.proxialertengine.dtos.OptInChannel;
import code.adagedo.proxialertengine.dtos.OptInStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@SuperBuilder
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
    @Enumerated(EnumType.STRING)
    private OptInChannel channel;

    @Column
    @Enumerated(EnumType.STRING)
    private OptInStatus optin_status;
}
