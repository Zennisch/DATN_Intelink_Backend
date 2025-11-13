package intelink.dto.auth;

public record ResetPasswordRequest(
        String password,
        String confirmPassword
) {
}
