package intelink.dto.request.url;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class UnlockUrlRequest {

    @NotBlank(message = "Password is required")
    private String password;
}
