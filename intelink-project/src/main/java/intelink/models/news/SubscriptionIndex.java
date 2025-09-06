package intelink.models.news;

import intelink.models.news.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscription_index")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubscriptionIndex {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID subscriptionId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    // Status group
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private SubscriptionStatus status;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Column(name = "next_process_at", nullable = false)
    private Instant nextProcessAt;

    // Audit group
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Lifecycle hooks
    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }

}
