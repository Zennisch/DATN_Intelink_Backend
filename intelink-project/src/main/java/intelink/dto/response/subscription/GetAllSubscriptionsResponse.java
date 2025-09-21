package intelink.dto.response.subscription;

import intelink.models.Subscription;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetAllSubscriptionsResponse {
    private List<SubscriptionResponse> subscriptions;
    private int total;

    public static GetAllSubscriptionsResponse fromEntities(List<Subscription> subscriptions) {
        List<SubscriptionResponse> responses = subscriptions.stream()
                .map(SubscriptionResponse::fromEntity)
                .toList();

        return GetAllSubscriptionsResponse.builder()
                .subscriptions(responses)
                .total(responses.size())
                .build();
    }
}
