package intelink.dto.auth;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdatePasswordRequest(
    @NotBlank
    String oldPassword,

    @NotBlank
    @Size(min = 6, max = 32)
    String newPassword
) {}
