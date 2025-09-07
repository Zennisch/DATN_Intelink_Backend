package intelink.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RegisterResponse {
    private boolean success;
    private String message;
    private String email;
    private boolean emailVerified;
}
