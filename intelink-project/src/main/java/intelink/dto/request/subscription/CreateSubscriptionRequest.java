package intelink.dto.request.subscription;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionRequest {
    @NotNull(message = "Subscription plan ID is required")
    private Long subscriptionPlanId;
}
