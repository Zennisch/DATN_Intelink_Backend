package intelink.dto.response.subscription;

import intelink.models.SubscriptionPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllSubscriptionPlansResponse {
    private List<SubscriptionPlanResponse> plans;
    private int total;

    public static GetAllSubscriptionPlansResponse fromEntities(List<SubscriptionPlan> plans) {
        List<SubscriptionPlanResponse> responses = plans.stream()
                .map(SubscriptionPlanResponse::fromEntity)
                .toList();
        
        return GetAllSubscriptionPlansResponse.builder()
                .plans(responses)
                .total(responses.size())
                .build();
    }
}
