package intelink.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ResetPasswordResponse {
    private boolean success;
    private String message;
}
