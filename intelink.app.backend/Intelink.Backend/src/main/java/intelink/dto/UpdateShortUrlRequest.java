package intelink.dto;

import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.time.Instant;

@Data
public class UpdateShortUrlRequest {

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Instant expiresAt;

    @Min(value = 1, message = "Max usage must be positive")
    private Long maxUsage;

    @Size(max = 100, message = "Password must not exceed 100 characters")
    private String password;

    private Boolean isActive;
}