package intelink.dto.subscription;

import intelink.models.SubscriptionPlan;
import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SubscriptionPlanResponse {
    private Long id;
    private SubscriptionPlanType type;
    private SubscriptionPlanBillingInterval billingInterval;
    private Boolean active;
    private String description;
    private Double price;
    private Integer maxShortUrls;
    private Integer maxUsagePerUrl;
    private Boolean shortCodeCustomizationEnabled;
    private Boolean statisticsEnabled;
    private Boolean apiAccessEnabled;
    private Instant createdAt;

    public static SubscriptionPlanResponse fromEntity(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .type(plan.getType())
                .billingInterval(plan.getBillingInterval())
                .active(plan.getActive())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .maxShortUrls(plan.getMaxShortUrls())
                .maxUsagePerUrl(plan.getMaxUsagePerUrl())
                .shortCodeCustomizationEnabled(plan.getShortCodeCustomizationEnabled())
                .statisticsEnabled(plan.getStatisticsEnabled())
                .apiAccessEnabled(plan.getApiAccessEnabled())
                .createdAt(plan.getCreatedAt())
                .build();
    }
}
