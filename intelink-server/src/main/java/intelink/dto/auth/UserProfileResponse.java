package intelink.dto.auth;

import intelink.models.Subscription;
import intelink.models.User;

public record UserProfileResponse(
        Long id,
        String username,
        String email,
        Boolean verified,
        String role,
        String status,
        String lastLoginAt,
        String profileName,
        String profilePictureUrl,
        Integer totalShortUrls,
        Long totalClicks,
        Double balance,
        String currency,
        String createdAt,
        String updatedAt,
        CurrentSubscription currentSubscription
) {
    public record CurrentSubscription(
            String id,
            String planType,
            String status,
            Boolean active,
            String activatedAt,
            String expiresAt,
            Double creditUsed,
            Double proratedValue
    ) {}

    public static UserProfileResponse fromEntity(User user, Subscription subscription) {
        CurrentSubscription currentSub = null;
        
        if (subscription != null) {
            currentSub = new CurrentSubscription(
                    subscription.getId().toString(),
                    subscription.getSubscriptionPlan().getType().name(),
                    subscription.getStatus().name(),
                    subscription.getActive(),
                    subscription.getActivatedAt() != null ? subscription.getActivatedAt().toString() : null,
                    subscription.getExpiresAt() != null ? subscription.getExpiresAt().toString() : null,
                    subscription.getCreditUsed(),
                    subscription.getProratedValue()
            );
        }
        
        return new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getVerified(),
                user.getRole().toString(),
                user.getStatus().toString(),
                user.getLastLoginAt() != null ? user.getLastLoginAt().toString() : null,
                user.getProfileName(),
                user.getProfilePictureUrl(),
                user.getTotalShortUrls(),
                user.getTotalClicks(),
                user.getBalance(),
                user.getCurrency(),
                user.getCreatedAt().toString(),
                user.getUpdatedAt().toString(),
                currentSub
        );
    }
}
