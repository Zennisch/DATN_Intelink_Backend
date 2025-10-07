package intelink.dto.request.payment;

import lombok.Data;

import java.math.BigDecimal;
import java.util.Map;
import java.util.UUID;

@Data
public class VnpayPaymentRequest {
    private UUID subscriptionId;
    private BigDecimal amount;
    private String currency;
    private Map<String, String> billingInfo;
}
