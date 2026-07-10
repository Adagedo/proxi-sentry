package code.adagedo.proxialertengine.models;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.sql.Timestamp;
import java.time.Instant;


@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(
        name = "processed_eonet_events",
        indexes = {
                @Index(name = "idx_eonet_id", columnList = "eonet_id", unique = true),
        },
        schema = "RECORD"
)
public class ProcessedDisaster{

    @Id
    @Column(name = "eonet_id")
    private String eonetId;

    @Column
    private String title;

    @Column
    private String description;

    @Builder.Default
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt = Timestamp.from(Instant.now());

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = true)
    private Timestamp updateAt;
}
