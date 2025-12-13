package intelink.modules.payment.controllers;

import intelink.dto.payment.CreateVNPayPaymentRequest;
import intelink.dto.payment.CreateVNPayPaymentResponse;
import intelink.models.Payment;
import intelink.models.Subscription;
import intelink.models.User;
import intelink.models.enums.PaymentStatus;
import intelink.models.enums.SubscriptionStatus;
import intelink.modules.auth.services.AuthService;
import intelink.modules.payment.services.PaymentService;
import intelink.modules.subscription.services.SubscriptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Payment", description = "Payment management endpoints")
public class PaymentController {

    private final PaymentService paymentService;
    private final SubscriptionService subscriptionService;
    private final AuthService authService;

    @PostMapping("/vnpay/create")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create VNPay payment", description = "Generate VNPay payment URL for subscription")
    public ResponseEntity<CreateVNPayPaymentResponse> createVNPayPayment(
            @Valid @RequestBody CreateVNPayPaymentRequest request) {
        
        User currentUser = authService.getCurrentUser();
        log.info("[PaymentController.createVNPayPayment] User {} creating payment for subscription {}", 
                currentUser.getId(), request.subscriptionId());

        CreateVNPayPaymentResponse response = paymentService.createVNPayPayment(request);
        
        if ("00".equals(response.getCode())) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/vnpay/callback")
    @Operation(summary = "VNPay payment callback", description = "Handle VNPay payment callback")
    public ResponseEntity<Map<String, Object>> vnpayCallback(@RequestParam Map<String, String> params) {
        log.info("[PaymentController.vnpayCallback] Received callback with params: {}", params.keySet());

        Map<String, Object> response = new HashMap<>();

        try {
            // Verify signature
            boolean isValid = paymentService.verifyVNPayCallback(params);
            if (!isValid) {
                log.warn("[PaymentController.vnpayCallback] Invalid signature");
                response.put("RspCode", "97");
                response.put("Message", "Invalid signature");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            // Get transaction reference
            String vnpTxnRef = params.get("vnp_TxnRef");
            String vnpResponseCode = params.get("vnp_ResponseCode");
            String vnpTransactionNo = params.get("vnp_TransactionNo");

            log.info("[PaymentController.vnpayCallback] TxnRef: {}, ResponseCode: {}, TransactionNo: {}", 
                    vnpTxnRef, vnpResponseCode, vnpTransactionNo);

            // Find payment by transaction ID
            Payment payment = paymentService.findByTransactionIdWithSubscription(vnpTxnRef);
            if (payment == null) {
                log.warn("[PaymentController.vnpayCallback] Payment not found for txnRef: {}", vnpTxnRef);
                response.put("RspCode", "01");
                response.put("Message", "Payment not found");
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
            }

            // Check if payment is already processed (idempotency)
            if (payment.getStatus() == PaymentStatus.COMPLETED) {
                log.info("[PaymentController.vnpayCallback] Payment already processed: {}", payment.getId());
                response.put("RspCode", "00");
                response.put("Message", "Payment already processed");
                return ResponseEntity.ok(response);
            }

            // Process payment based on response code
            if ("00".equals(vnpResponseCode)) {
                // Payment successful
                processSuccessfulPayment(payment, vnpTransactionNo);
                
                response.put("RspCode", "00");
                response.put("Message", "Success");
                return ResponseEntity.ok(response);
                
            } else {
                // Payment failed
                processFailedPayment(payment, vnpResponseCode);
                
                response.put("RspCode", "00");
                response.put("Message", "Payment confirmed as failed");
                return ResponseEntity.ok(response);
            }

        } catch (Exception e) {
            log.error("[PaymentController.vnpayCallback] Error processing callback: {}", e.getMessage(), e);
            response.put("RspCode", "99");
            response.put("Message", "System error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Process successful payment and activate subscription
     */
    private void processSuccessfulPayment(Payment payment, String vnpTransactionNo) {
        log.info("[PaymentController.processSuccessfulPayment] Processing payment: {}", payment.getId());

        // Update payment status
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setMetadata(payment.getMetadata() + "|vnp_TransactionNo=" + vnpTransactionNo);
        paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.COMPLETED);

        // Activate subscription
        Subscription subscription = payment.getSubscription();
        if (subscription != null && subscription.getStatus() == SubscriptionStatus.PENDING) {
            try {
                subscriptionService.activateSubscription(subscription.getId(), subscription.getUser());
                log.info("[PaymentController.processSuccessfulPayment] Activated subscription: {}", subscription.getId());
            } catch (Exception e) {
                log.error("[PaymentController.processSuccessfulPayment] Failed to activate subscription: {}", 
                        e.getMessage(), e);
                // Payment is completed but subscription activation failed
                // This should trigger a manual review or retry mechanism
                throw new RuntimeException("Payment completed but subscription activation failed", e);
            }
        }
    }

    /**
     * Process failed payment
     */
    private void processFailedPayment(Payment payment, String responseCode) {
        log.info("[PaymentController.processFailedPayment] Processing failed payment: {}, code: {}", 
                payment.getId(), responseCode);

        // Update payment status
        payment.setStatus(PaymentStatus.FAILED);
        payment.setMetadata(payment.getMetadata() + "|failureCode=" + responseCode);
        paymentService.updatePaymentStatus(payment.getId(), PaymentStatus.FAILED);

        // Cancel subscription
        Subscription subscription = payment.getSubscription();
        if (subscription != null && subscription.getStatus() == SubscriptionStatus.PENDING) {
            subscription.setStatus(SubscriptionStatus.CANCELED);
            log.info("[PaymentController.processFailedPayment] Canceled subscription: {}", subscription.getId());
        }
    }
}
