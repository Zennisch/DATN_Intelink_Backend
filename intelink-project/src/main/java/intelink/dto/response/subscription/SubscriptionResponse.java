package intelink.dto.response.subscription;

import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.enums.SubscriptionPlanType;
import intelink.models.enums.SubscriptionStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    // Subscription info
    private UUID id;
    private SubscriptionStatus status;
    private Boolean active;
    private Instant createdAt;
    private Instant startsAt;
    private Instant expiresAt;

    // Plan info
    private Long planId;
    private SubscriptionPlanType planType;
    private String planDescription;
    private BigDecimal planPrice;
    private Integer maxShortUrls;
    private Boolean shortCodeCustomizationEnabled;
    private Boolean statisticsEnabled;
    private Boolean customDomainEnabled;
    private Boolean apiAccessEnabled;

    public static SubscriptionResponse fromEntity(Subscription subscription) {
        SubscriptionPlan plan = subscription.getSubscriptionPlan();

        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .status(subscription.getStatus())
                .active(subscription.getActive())
                .createdAt(subscription.getCreatedAt())
                .startsAt(subscription.getStartsAt())
                .expiresAt(subscription.getExpiresAt())
                .planId(plan.getId())
                .planType(plan.getType())
                .planDescription(plan.getDescription())
                .planPrice(plan.getPrice())
                .maxShortUrls(plan.getMaxShortUrls())
                .shortCodeCustomizationEnabled(plan.getShortCodeCustomizationEnabled())
                .statisticsEnabled(plan.getStatisticsEnabled())
                .customDomainEnabled(plan.getCustomDomainEnabled())
                .apiAccessEnabled(plan.getApiAccessEnabled())
                .build();
    }
}
