package intelink.controllers;

import intelink.dto.request.payment.VnpayPaymentRequest;
import intelink.models.Payment;
import intelink.models.Subscription;
import intelink.models.enums.PaymentStatus;
import intelink.repositories.SubscriptionRepository;
import intelink.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
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
}
