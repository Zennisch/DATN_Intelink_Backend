package intelink.dto.response.auth;

import intelink.dto.object.SubscriptionInfo;
import intelink.models.User;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class UserProfileResponse {
    // Basic user info
    private Long id;
    private String username;
    private String email;
    private String role;
    private String status;

    // Profile info
    private String displayName;
    private String bio;
    private String profilePictureUrl;

    // Auth info
    private Boolean emailVerified;
    private String authProvider;
    private String providerUserId;
    private Instant lastLoginAt;

    // Payment
    private Double creditBalance;
    private String currency;

    // Statistics
    private Long totalClicks;
    private Integer totalShortUrls;

    // Current subscription info
    private SubscriptionInfo currentSubscription;

    // Audit
    private Instant createdAt;
    private Instant updatedAt;

    public static UserProfileResponse fromEntities(User user, SubscriptionInfo subscriptionInfo) {
        return UserProfileResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .role(user.getRole().toString())
                .status(user.getStatus().toString())
                .displayName(user.getDisplayName())
                .bio(user.getBio())
                .profilePictureUrl(user.getProfilePictureUrl())
                .emailVerified(user.getEmailVerified())
                .authProvider(user.getProvider().toString())
                .providerUserId(user.getProviderUserId())
                .lastLoginAt(user.getLastLoginAt())
                .creditBalance(user.getCreditBalance())
                .currency(user.getCurrency())
                .totalClicks(user.getTotalClicks())
                .totalShortUrls(user.getTotalShortUrls())
                .currentSubscription(subscriptionInfo)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}