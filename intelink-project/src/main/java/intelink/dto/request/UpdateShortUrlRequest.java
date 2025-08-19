package intelink.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateShortUrlRequest {

    @Size(max = 255, message = "Description must not exceed 255 characters")
    private String description;

    @Min(value = 0, message = "Max usage must be non-negative")
    private Long maxUsage;

    @Min(value = 1, message = "Available days must be positive")
    private Integer availableDays;
}
