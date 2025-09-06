package intelink.models.news;

import com.fasterxml.jackson.annotation.JsonIgnore;
import intelink.models.news.enums.SubscriptionPlanBillingInterval;
import intelink.models.news.enums.SubscriptionPlanType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Entity
@Entity
@Table(name = "subscription_plans", indexes = {
        @Index(name = "idx_subscription_plans_type", columnList = "type"),
        @Index(name = "idx_subscription_plans_active", columnList = "active"),
        @Index(name = "idx_subscription_plans_active_type", columnList = "active, type")
})
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

    @OneToMany(mappedBy = "subscriptionPlan", fetch = FetchType.LAZY)
    @ToString.Exclude
    @JsonIgnore
    private List<Subscription> subscriptions;

    // Details group
    @Column(name = "type", nullable = false, unique = true, length = 32)
    private SubscriptionPlanType type;

    @Column(name = "description", nullable = true, length = 1024)
    private String description;

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
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

    // Configuration group
    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    // Lifecycle hooks
    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
    }
}
