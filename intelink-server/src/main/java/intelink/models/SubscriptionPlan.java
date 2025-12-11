package intelink.models;

import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder

@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, unique = true, length = 32)
    private SubscriptionPlanType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_interval", nullable = false, length = 16)
    private SubscriptionPlanBillingInterval billingInterval;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "description", length = 1024)
    private String description;

    @Column(name = "price", nullable = false)
    private Double price;

    @Column(name = "max_short_urls", nullable = false)
    private Integer maxShortUrls;

    @Column(name = "max_usage_per_url", nullable = false)
    private Integer maxUsagePerUrl;

    @Column(name = "short_code_customization_enabled", nullable = false)
    private Boolean shortCodeCustomizationEnabled;

    @Column(name = "statistics_enabled", nullable = false)
    private Boolean statisticsEnabled;

    @Column(name = "api_access_enabled", nullable = false)
    private Boolean apiAccessEnabled;

    @Column(name = "duration")
    private Integer duration;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
    }

}
