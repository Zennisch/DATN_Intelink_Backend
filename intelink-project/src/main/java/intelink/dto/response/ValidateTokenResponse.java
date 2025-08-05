package intelink.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ValidateTokenResponse {

    private boolean valid;
    private String username;
    private String role;
    private Long expiresIn;
    private String message;

}
