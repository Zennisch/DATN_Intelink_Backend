package intelink.dto.subscription;

import intelink.models.Subscription;
import intelink.models.enums.SubscriptionStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class SubscriptionResponse {
    private UUID id;
    private Long userId;
    private Long planId;
    private String planType;
    private SubscriptionStatus status;
    private Boolean active;
    private Double creditUsed;
    private Double proratedValue;
    private Instant activatedAt;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static SubscriptionResponse fromEntity(Subscription subscription) {
        return SubscriptionResponse.builder()
                .id(subscription.getId())
                .userId(subscription.getUser() != null ? subscription.getUser().getId() : null)
                .planId(subscription.getSubscriptionPlan() != null ? subscription.getSubscriptionPlan().getId() : null)
                .planType(subscription.getSubscriptionPlan() != null ? subscription.getSubscriptionPlan().getType().name() : null)
                .status(subscription.getStatus())
                .active(subscription.getActive())
                .creditUsed(subscription.getCreditUsed())
                .proratedValue(subscription.getProratedValue())
                .activatedAt(subscription.getActivatedAt())
                .expiresAt(subscription.getExpiresAt())
                .createdAt(subscription.getCreatedAt())
                .updatedAt(subscription.getUpdatedAt())
                .build();
    }
}
