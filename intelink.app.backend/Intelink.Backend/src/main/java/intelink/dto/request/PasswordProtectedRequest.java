package intelink.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class PasswordProtectedRequest {

    @NotBlank(message = "Password is required")
    private String password;
}