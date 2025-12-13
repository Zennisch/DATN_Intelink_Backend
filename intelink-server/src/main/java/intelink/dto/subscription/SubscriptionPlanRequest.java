package intelink.dto.subscription;

import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SubscriptionPlanRequest(
        @NotNull(message = "Plan type is required")
        SubscriptionPlanType type,

        @NotNull(message = "Billing interval is required")
        SubscriptionPlanBillingInterval billingInterval,

        @Size(max = 1024, message = "Description must not exceed 1024 characters")
        String description,

        @NotNull(message = "Price is required")
        @Min(value = 0, message = "Price must be non-negative")
        Double price,

        @NotNull(message = "Max short URLs is required")
        @Min(value = 1, message = "Max short URLs must be at least 1")
        Integer maxShortUrls,

        @NotNull(message = "Max usage per URL is required")
        @Min(value = 1, message = "Max usage per URL must be at least 1")
        Integer maxUsagePerUrl,

        @NotNull(message = "Short code customization enabled flag is required")
        Boolean shortCodeCustomizationEnabled,

        @NotNull(message = "Statistics enabled flag is required")
        Boolean statisticsEnabled,

        @NotNull(message = "API access enabled flag is required")
        Boolean apiAccessEnabled
) {
}
