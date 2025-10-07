package intelink.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Table(name = "subscription_plans")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class SubscriptionPlan {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToMany(mappedBy = "subscriptionPlan", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @JsonIgnore
    private List<Subscription> subscriptions;

    // Details group
    @Column(name = "type", nullable = false, unique = true, length = 32)
    @Enumerated(EnumType.STRING)
    private SubscriptionPlanType type;

    @Column(name = "description", nullable = true, length = 1024)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    @NotNull
    @DecimalMin(value = "0.00", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_interval", nullable = false, length = 16)
    private SubscriptionPlanBillingInterval billingInterval;

    // Features group
    @Column(name = "max_short_urls", nullable = false)
    private Integer maxShortUrls;

    @Column(name = "short_code_customization_enabled", nullable = false)
    private Boolean shortCodeCustomizationEnabled;

    @Column(name = "statistics_enabled", nullable = false)
    private Boolean statisticsEnabled;

    @Column(name = "custom_domain_enabled", nullable = false)
    private Boolean customDomainEnabled;

    @Column(name = "api_access_enabled", nullable = false)
    private Boolean apiAccessEnabled;

    // Audit group
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "max_usage_per_url", nullable = false)
    private Integer max_usage_per_url;

    // Lifecycle hooks
    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }
}
