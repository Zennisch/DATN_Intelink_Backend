package intelink.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private boolean success;
    private String message;
    private String email;
    private boolean emailVerified;
}
