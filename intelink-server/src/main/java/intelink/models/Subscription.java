package intelink.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder

@Entity
@Table(name = "subscriptions")
public class Subscription {

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_plan_id", nullable = false)
    @ToString.Exclude
    @JsonIgnore
    private SubscriptionPlan subscriptionPlan;

    @OneToOne(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private Payment payment;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    private SubscriptionStatus status;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = false;

    @Column(name = "credit_used", nullable = false)
    @Builder.Default
    private Double creditUsed = 0.0;

    @Column(name = "prorated_value")
    private Double proratedValue;

    @Column(name = "activated_at")
    private Instant activatedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
