package intelink.dto.auth;

import intelink.utils.helper.AuthToken;

public record AuthTokenResponse(
        String token,
        String refreshToken,
        String username,
        String email,
        String role,
        Long expiresAt
) {
    public static AuthTokenResponse fromEntity(AuthToken obj) {
        return new AuthTokenResponse(
                obj.token(),
                obj.refreshToken(),
                obj.user().getUsername(),
                obj.user().getEmail(),
                obj.user().getRole().toString(),
                obj.expiresAt()
        );
    }
}
