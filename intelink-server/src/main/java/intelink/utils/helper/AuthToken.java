package intelink.utils.helper;

import intelink.models.User;

public record AuthToken(User user, String token, String refreshToken, Long expiresAt) {
}
