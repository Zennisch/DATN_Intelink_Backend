package intelink.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.enums.PaymentProvider;
import intelink.models.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
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
@Table(name = "payments")
public class Payment {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false, unique = true)
    @ToString.Exclude
    @JsonIgnore
    private Subscription subscription;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 16)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "amount", nullable = false)
    @Min(value = 0)
    private Double amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    @Column(name = "transaction_id", unique = true)
    private String transactionId;

    @Column(name = "metadata", length = 2048)
    private String metadata;

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
