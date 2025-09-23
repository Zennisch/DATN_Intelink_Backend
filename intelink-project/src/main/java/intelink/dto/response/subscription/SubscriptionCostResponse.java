package intelink.dto.response.subscription;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class SubscriptionCostResponse {
    private Long subscriptionPlanId;
    private BigDecimal planPrice;
    private BigDecimal proRateValue;
    private BigDecimal amountToPay;
    private double creditBalance;
    private String currency;
    private String message;
    private Instant startDate;
}