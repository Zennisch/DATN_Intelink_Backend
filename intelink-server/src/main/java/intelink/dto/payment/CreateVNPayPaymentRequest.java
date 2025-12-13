package intelink.dto.payment;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateVNPayPaymentRequest(
        @NotNull(message = "Subscription ID is required")
        UUID subscriptionId,

        @NotNull(message = "Amount is required")
        @Min(value = 1, message = "Amount must be at least 1")
        Double amount,

        @NotNull(message = "Currency is required")
        @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be a valid ISO 4217 code (e.g., VND, USD)")
        @Size(min = 3, max = 3, message = "Currency must be exactly 3 characters")
        String currency,

        @Valid
        BillingInfo billingInfo
) {
}
