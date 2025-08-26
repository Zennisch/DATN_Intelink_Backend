package intelink.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "premium_plans", indexes = {
        @Index(name = "idx_premium_plans_name", columnList = "name", unique = true),
        @Index(name = "idx_premium_plans_active", columnList = "active")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class PremiumPlan {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "name", nullable = false, unique = true, length = 50)
    private String name;

    @Column(name = "description", nullable = true, length = 500)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "duration_days", nullable = false)
    private Integer durationDays;

    @Column(name = "max_short_urls", nullable = false)
    private Integer maxShortUrls;

    @Builder.Default
    @Column(name = "custom_domain_enabled", nullable = false)
    private Boolean customDomainEnabled = true;

    @Builder.Default
    @Column(name = "analytics_enabled", nullable = false)
    private Boolean analyticsEnabled = true;

    @Builder.Default
    @Column(name = "api_access_enabled", nullable = false)
    private Boolean apiAccessEnabled = true;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
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
