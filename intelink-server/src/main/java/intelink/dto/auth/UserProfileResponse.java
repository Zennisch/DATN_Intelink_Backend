package intelink.dto.auth;

import intelink.models.User;

public record UserProfileResponse(
        User user
) {
}
