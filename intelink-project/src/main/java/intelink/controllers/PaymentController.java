package intelink.controllers;

import intelink.dto.request.payment.VnpayPaymentRequest;
import intelink.models.Subscription;
import intelink.repositories.SubscriptionRepository;
import intelink.services.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/payment")
@RequiredArgsConstructor
public class PaymentController {
    private final PaymentService paymentService;
    private final SubscriptionRepository subscriptionRepository;

    @PostMapping("/vnpay")
    public Map<String, Object> createVnpayPayment(@RequestBody VnpayPaymentRequest request) {
        try {
            UUID subscriptionId = request.getSubscriptionId();
            Optional<Subscription> subscriptionOpt = subscriptionRepository.findById(subscriptionId);
            if (subscriptionOpt.isEmpty()) {
                Map<String, Object> result = new java.util.HashMap<>();
                result.put("code", "01");
                result.put("message", "Subscription not found");
                result.put("data", null);
                return result;
            }
            Subscription subscription = subscriptionOpt.get();

            String paymentUrl = paymentService.createVnpayPayment(
                    subscription,
                    request.getAmount(),
                    request.getCurrency(),
                    request.getBillingInfo()
            );
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", "00");
            result.put("message", "success");
            result.put("data", paymentUrl);
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new java.util.HashMap<>();
            result.put("code", "99");
            result.put("message", "error: " + e.getMessage());
            result.put("data", null);
            return result;
        }
    }
}
