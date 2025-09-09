package intelink.dto.response.subscription;

import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class SubscriptionPlanResponse {
    private Long id;
    private SubscriptionPlanType type;
    private String description;
    private BigDecimal price;
    private SubscriptionPlanBillingInterval billingInterval;
    private Integer maxShortUrls;
    private Boolean shortCodeCustomizationEnabled;
    private Boolean statisticsEnabled;
    private Boolean customDomainEnabled;
    private Boolean apiAccessEnabled;
    private Boolean active;
    private Instant createdAt;
    private Integer maxUsagePerUrl;
}


