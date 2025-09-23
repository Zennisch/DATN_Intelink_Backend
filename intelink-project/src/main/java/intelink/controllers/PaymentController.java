package intelink.controllers;

import intelink.dto.request.payment.VnpayPaymentRequest;
import intelink.models.Payment;
import intelink.models.Subscription;
import intelink.models.enums.PaymentStatus;
import intelink.models.enums.SubscriptionStatus;
import intelink.repositories.SubscriptionRepository;
import intelink.services.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
@Slf4j
public class PaymentController {
    private final PaymentService paymentService;
    private final SubscriptionRepository subscriptionRepository;

    @PostMapping("/vnpay")
    public Map<String, Object> createVnpayPayment(@RequestBody VnpayPaymentRequest request) {
        return paymentService.handleVnpayPaymentRequest(request);
    }

    @GetMapping
    public Map<String, Object> getAllPayments() {
        List<Payment> payments = paymentService.getAllPayments();
        Map<String, Object> result = new HashMap<>();
        result.put("data", payments);
        result.put("count", payments.size());
        result.put("code", "00");
        result.put("message", "success");
        return result;
    }

    @GetMapping("/{id}")
    public Payment getPaymentById(@PathVariable UUID id) {
        return paymentService.findPaymentById(id).orElse(null);
    }

    @PutMapping("/{id}")
    public Payment updatePayment(@PathVariable UUID id, @RequestBody Payment payment) {
        payment.setId(id);
        return paymentService.updatePayment(payment).orElse(null);
    }

    @DeleteMapping("/{id}")
    public boolean deletePayment(@PathVariable UUID id) {
        return paymentService.deletePayment(id);
    }

    @PatchMapping("/{id}/status")
    public Payment updatePaymentStatus(@PathVariable UUID id, @RequestParam PaymentStatus status) {
        return paymentService.updatePaymentStatus(id, status).orElse(null);
    }

    @PostMapping("/vnpay/callback")
    public ResponseEntity<?> handleVnpayCallback(@RequestBody Map<String, String> params) {
        // Xác thực chữ ký, kiểm tra trạng thái giao dịch
        boolean isValid = paymentService.verifyVnpayCallback(params);
        // if (!isValid) {
        //     return ResponseEntity.badRequest().body("Invalid payment callback");
        // }

        // Lấy payment theo transactionId hoặc subscriptionId
        Payment payment = paymentService.findByTransactionId(params.get("vnp_TxnRef"));
        if (payment == null) {
            return ResponseEntity.badRequest().body("Payment not found");
        }

        // Cập nhật trạng thái payment
        payment.setStatus(PaymentStatus.COMPLETED);
        paymentService.updatePayment(payment);

        // Kích hoạt subscription
        log.info("Activating subscription for payment ID: {}", payment.getId());
        Subscription subscription = paymentService.getSubscriptionByPaymentId(payment.getId());
        log.info("Subscription before activation: {}", subscription);
        subscription.setStatus(SubscriptionStatus.ACTIVE);
        subscription.setActive(true);
        subscription.setStartsAt(Instant.now());
        subscriptionRepository.save(subscription);

        return ResponseEntity.ok("Payment and subscription activated");
    }
}
