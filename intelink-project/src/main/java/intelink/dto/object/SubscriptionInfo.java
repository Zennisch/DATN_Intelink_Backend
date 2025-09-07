package intelink.dto.object;

import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.enums.SubscriptionPlanType;
import intelink.models.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SubscriptionInfo {
    // Subscription details
    private UUID subscriptionId;
    private SubscriptionPlanType planType;
    private String planDescription;
    private SubscriptionStatus status;
    private Boolean active;
    private Instant startsAt;
    private Instant expiresAt;

    // Plan features
    private Integer maxShortUrls;
    private Boolean shortCodeCustomizationEnabled;
    private Boolean statisticsEnabled;
    private Boolean customDomainEnabled;
    private Boolean apiAccessEnabled;

    public static SubscriptionInfo fromEntities(Subscription subscription, SubscriptionPlan plan) {
        return SubscriptionInfo.builder()
                .subscriptionId(subscription.getId())
                .planType(plan.getType())
                .planDescription(plan.getDescription())
                .status(subscription.getStatus())
                .active(subscription.getStatus() == SubscriptionStatus.ACTIVE)
                .startsAt(subscription.getStartsAt())
                .expiresAt(subscription.getExpiresAt())
                .maxShortUrls(plan.getMaxShortUrls())
                .shortCodeCustomizationEnabled(plan.getShortCodeCustomizationEnabled())
                .statisticsEnabled(plan.getStatisticsEnabled())
                .customDomainEnabled(plan.getCustomDomainEnabled())
                .apiAccessEnabled(plan.getApiAccessEnabled())
                .build();
    }
}
