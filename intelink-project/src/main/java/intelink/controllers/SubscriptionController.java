package intelink.controllers;

import intelink.dto.request.subscription.RegisterSubscriptionRequest;
import intelink.dto.response.subscription.CancelSubscriptionResponse;
import intelink.dto.response.subscription.GetAllSubscriptionsResponse;
import intelink.dto.response.subscription.SubscriptionResponse;
import intelink.models.Subscription;
import intelink.models.User;
import intelink.services.PaymentService;
import intelink.services.interfaces.ISubscriptionService;
import intelink.services.interfaces.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;
    private final IUserService userService;
    private final PaymentService paymentService;

    @GetMapping
    public ResponseEntity<?> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails);
        GetAllSubscriptionsResponse response = subscriptionService.findByUser(user);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSubscription(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails);
        SubscriptionResponse response = subscriptionService.getCurrentActiveSubscriptionForUser(user);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> registerSubscription(
            @Valid @RequestBody RegisterSubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) throws Exception {
        User user = userService.getCurrentUser(userDetails);
        // Tạo subscription ở trạng thái PENDING
        Subscription subscription = subscriptionService.createPendingSubscription(user, request);

        // Tạo payment và lấy payment URL
        BigDecimal amountToPay = subscriptionService.calculateAmountToPay(user, subscription, request);
        String paymentUrl = paymentService.createVnpayPayment(subscription, amountToPay, user.getCurrency(),
                new HashMap<>());

        // Trả về payment URL cho frontend
        return ResponseEntity.ok(Map.of(
                "subscriptionId", subscription.getId(),
                "paymentUrl", paymentUrl));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails);
        subscriptionService.cancelSubscription(user, id);

        return ResponseEntity.ok(CancelSubscriptionResponse.builder()
                .success(true)
                .message("Subscription cancelled successfully")
                .subscriptionId(id)
                .build());
    }
}
