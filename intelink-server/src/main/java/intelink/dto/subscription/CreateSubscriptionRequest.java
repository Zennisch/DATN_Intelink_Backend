package intelink.dto.subscription;

import jakarta.validation.constraints.NotNull;

public record CreateSubscriptionRequest(
        @NotNull(message = "Plan ID is required")
        Long planId
) {
}
