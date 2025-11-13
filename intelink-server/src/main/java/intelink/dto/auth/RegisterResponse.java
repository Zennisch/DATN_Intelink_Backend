package intelink.dto.auth;

public record RegisterResponse(
        boolean success,
        String message,
        String email,
        boolean emailVerified
) {
}
