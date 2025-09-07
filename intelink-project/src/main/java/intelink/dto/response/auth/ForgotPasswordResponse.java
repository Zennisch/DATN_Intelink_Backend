package intelink.dto.response.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ForgotPasswordResponse {
    private boolean success;
    private String message;
}
