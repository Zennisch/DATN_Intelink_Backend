package intelink.dto.request.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterSubscriptionRequest {
    @NotNull(message = "Subscription plan ID is required")
    private Long subscriptionPlanId;

    private Boolean autoRenew = false;

    private Boolean applyImmediately = false;
}
