package intelink.dto.response.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class VerifyEmailResponse {
    private boolean success;
    private String message;
}
