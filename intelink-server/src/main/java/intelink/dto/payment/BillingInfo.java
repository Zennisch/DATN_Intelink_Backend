package intelink.dto.payment;

import jakarta.validation.constraints.Email;

public record BillingInfo(
        String ip,
        String mobile,
        
        @Email(message = "Invalid email format")
        String email,
        
        String firstName,
        String lastName,
        String address,
        String city,
        String country
) {
}
