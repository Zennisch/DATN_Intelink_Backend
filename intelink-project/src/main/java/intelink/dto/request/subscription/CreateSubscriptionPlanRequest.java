package intelink.dto.request.subscription;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSubscriptionPlanRequest {

    @NotBlank(message = "Plan type is required")
    @Size(max = 50, message = "Plan type must not exceed 50 characters")
    private String type;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Price must be non-negative")
    private BigDecimal price;

    @NotBlank(message = "Billing interval is required")
    private String billingInterval;

    @NotNull(message = "Max short URLs is required")
    @Min(value = 0, message = "Max short URLs must be non-negative")
    private Integer maxShortUrls;

    @NotNull(message = "Max usage per URL is required")
    @Min(value = 0, message = "Max usage per URL must be non-negative")
    private Integer maxUsagePerUrl;

    @Builder.Default
    private Boolean shortCodeCustomizationEnabled = false;

    @Builder.Default
    private Boolean statisticsEnabled = false;

    @Builder.Default
    private Boolean customDomainEnabled = false;

    @Builder.Default
    private Boolean apiAccessEnabled = false;

    @Builder.Default
    private Boolean active = true;
}
