package intelink.dto.subscription;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class CalculateCostResponse {
    private Long planId;
    private String planType;
    private Double planPrice;
    private Integer durationDays;
    
    private Double proratedCredit;
    private String currentPlanType;
    private Instant currentExpiresAt;
    
    private Double finalCost;
    private Double savings;
    
    private String message;
}
