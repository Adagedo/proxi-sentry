package code.adagedo.proxialertengine.models;


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
        name = "users",
        indexes = {
                @Index(name = "idx_user_id", columnList = "id", unique = true),
                @Index(name = "idx_user_email", columnList = "email", unique = true),
                @Index(name = "idx_user_long", columnList = "longitude"),
                @Index(name = "idx_user_lat", columnList = "latitude")
        },
        schema = "RECORD"
)

public class User extends BaseModel{

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "longitude")
    private long longitude;

    @Column(name = "latitude")
    private long latitude;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    @EqualsAndHashCode.Exclude
    private NotificationSetting notificationSetting;
}
