package intelink.dto.request.url;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.hibernate.validator.constraints.URL;

@Data
public class CreateShortUrlRequest {

    @NotBlank(message = "Original URL is required")
    @URL(message = "URL must be valid")
    @Size(max = 2048, message = "URL must not exceed 2048 characters")
    private String originalUrl;

    @Size(max = 128, message = "Password must not exceed 128 characters")
    private String password;

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Min(value = 0, message = "Max usage must be non-negative")
    private Long maxUsage;

    @NotNull(message = "Available days must be specified")
    @Min(value = 1, message = "Available days must be positive")
    private Integer availableDays;

}
