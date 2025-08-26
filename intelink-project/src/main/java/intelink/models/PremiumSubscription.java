package intelink.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "premium_subscriptions", indexes = {
        @Index(name = "idx_premium_subscriptions_user", columnList = "user_id"),
        @Index(name = "idx_premium_subscriptions_payment", columnList = "payment_id"),
        @Index(name = "idx_premium_subscriptions_expires_at", columnList = "expires_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class PremiumSubscription {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "premium_plan_id", nullable = false)
    @ToString.Exclude
    private PremiumPlan premiumPlan;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false)
    @ToString.Exclude
    private Payment payment;

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }

    public boolean isActive() {
        return active && Instant.now().isBefore(expiresAt);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }
}
