package intelink.dto.payment;

import intelink.models.Payment;
import intelink.models.enums.PaymentProvider;
import intelink.models.enums.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
public class PaymentResponse {
    private UUID id;
    private UUID subscriptionId;
    private PaymentProvider provider;
    private PaymentStatus status;
    private Double amount;
    private String currency;
    private String transactionId;
    private Instant processedAt;
    private Instant expiresAt;
    private Instant createdAt;
    private Instant updatedAt;

    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .subscriptionId(payment.getSubscription() != null ? payment.getSubscription().getId() : null)
                .provider(payment.getProvider())
                .status(payment.getStatus())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .transactionId(payment.getTransactionId())
                .processedAt(payment.getProcessedAt())
                .expiresAt(payment.getExpiresAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
