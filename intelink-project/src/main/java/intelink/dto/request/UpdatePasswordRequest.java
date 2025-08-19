package intelink.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdatePasswordRequest {

    @Size(max = 128, message = "Password must not exceed 128 characters")
    private String newPassword;

    private String currentPassword;
}
