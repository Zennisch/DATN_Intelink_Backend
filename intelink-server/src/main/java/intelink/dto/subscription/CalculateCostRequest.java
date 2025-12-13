package intelink.dto.subscription;

import jakarta.validation.constraints.NotNull;

public record CalculateCostRequest(
        @NotNull(message = "Plan ID is required")
        Long planId
) {
}
