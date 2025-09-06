package intelink.models.news;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.news.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class Subscription {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private User user;

    // SubscriptionPlan here
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private SubscriptionPlan subscriptionPlan;

    // Payment here
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    @ToString.Exclude
    @JsonIgnore
    private Payment payment;

    // Status group
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SubscriptionStatus status;

    // Audit group
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "starts_at", nullable = false)
    private Instant startsAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    // Lifecycle hooks
    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }
}
