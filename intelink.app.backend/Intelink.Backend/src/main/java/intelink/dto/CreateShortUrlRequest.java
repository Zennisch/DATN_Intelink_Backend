package intelink.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

import java.time.Instant;

@Data
public class CreateShortUrlRequest {

    @NotBlank(message = "Original URL is required")
    @URL(message = "URL must be valid")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String originalUrl;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    private Instant expiresAt;

    @Min(value = 1, message = "Max usage must be positive")
    private Long maxUsage;

    @Size(max = 100, message = "Password must not exceed 100 characters")
    private String password;
}