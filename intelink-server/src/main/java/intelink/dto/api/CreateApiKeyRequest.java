package intelink.dto.api;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateApiKeyRequest {
    @NotBlank(message = "Name is required")
    private String name;
}
