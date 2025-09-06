package intelink.models.news;

import intelink.models.news.enums.PaymentProvider;
import intelink.models.news.enums.PaymentStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class Payment {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    // Details group
    @Enumerated(EnumType.STRING)
    @Column(name = "provider", nullable = false, length = 16)
    private PaymentProvider provider;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 16)
    @Builder.Default
    private PaymentStatus status = PaymentStatus.PENDING;

    @Column(name = "amount", nullable = false)
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code")
    private String currency;

    // Result group
    @Column(name = "transaction_id", nullable = true, unique = true)
    private String transactionId;

    @Column(name = "metadata", nullable = true, columnDefinition = "TEXT")
    private String metadata;

    // Audit group
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = true)
    private Instant updatedAt;

    @Column(name = "processed_at", nullable = true)
    private Instant processedAt;

    @Column(name = "expires_at", nullable = true)
    private Instant expiresAt;

    // Lifecycle hooks
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
