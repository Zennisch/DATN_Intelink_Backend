package intelink.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

public record RegisterResponse(
        boolean success,
        String message,
        String email,
        boolean emailVerified
) {
}
