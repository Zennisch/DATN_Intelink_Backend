package intelink.dto.url;

import intelink.models.enums.AccessControlMode;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.hibernate.validator.constraints.URL;

import java.util.List;

public record CreateShortUrlRequest(
        @NotBlank(message = "Original URL is required")
        @URL(message = "URL must be valid")
        @Size(max = 2048, message = "URL must not exceed 2048 characters")
        String originalUrl,

        @Size(max = 32, message = "Title must not exceed 32 characters")
        String title,

        @Size(max = 255, message = "Description must not exceed 255 characters")
        String description,

        @Size(max = 32, message = "Custom code must not exceed 64 characters")
        String customCode,

        @Min(value = 1, message = "Available days must be positive")
        Integer availableDays,

        @Min(value = 0, message = "Max usage must be non-negative")
        Integer maxUsage,

        @Size(max = 128, message = "Password must not exceed 128 characters")
        String password,

        AccessControlMode accessControlMode,

        List<String> accessControlCIDRs,

        List<String> accessControlGeographies
) {
}
