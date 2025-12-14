package intelink.dto.auth;

import jakarta.validation.constraints.Size;

public record UpdateProfileRequest(
    @Size(min = 6, max = 32)
    String username,

    @Size(max = 64)
    String profileName,

    @Size(max = 256)
    String profilePictureUrl
) {}
