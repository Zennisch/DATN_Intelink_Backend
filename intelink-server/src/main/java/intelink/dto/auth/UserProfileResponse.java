package intelink.dto.auth;

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
        String updatedAt
) {
    public static UserProfileResponse fromEntity(User user) {
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
                user.getUpdatedAt().toString()
        );
    }
}
