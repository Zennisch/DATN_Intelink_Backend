package intelink.dto.payment;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateVNPayPaymentResponse {
    private String code;
    private String message;
    private String paymentUrl;
}
