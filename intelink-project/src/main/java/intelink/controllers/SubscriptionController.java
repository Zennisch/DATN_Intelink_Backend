package intelink.controllers;

import intelink.dto.request.subscription.RegisterSubscriptionRequest;
import intelink.dto.response.subscription.CancelSubscriptionResponse;
import intelink.dto.response.subscription.GetAllSubscriptionsResponse;
import intelink.dto.response.subscription.SubscriptionCostResponse;
import intelink.dto.response.subscription.SubscriptionResponse;
import intelink.models.Payment;
import intelink.models.Subscription;
import intelink.models.User;
import intelink.models.enums.PaymentProvider;
import intelink.models.enums.PaymentStatus;
import intelink.models.enums.SubscriptionStatus;
import intelink.repositories.SubscriptionRepository;
import intelink.repositories.UserRepository;
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
    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;

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
        Subscription subscription = subscriptionService.createPendingSubscription(user, request);

        SubscriptionCostResponse costResponse = subscriptionService.calculateSubscriptionCost(user,
                request.getSubscriptionPlanId(), request.getApplyImmediately());
        BigDecimal amountToPay = costResponse.getAmountToPay();

        log.info("Registering subscription for user: {}, planId: {}, applyImmediately: {}, amountToPay: {}",
                user.getUsername(), request.getSubscriptionPlanId(), request.getApplyImmediately(), amountToPay);
        if (amountToPay.compareTo(BigDecimal.ZERO) <= 0) {
            amountToPay = BigDecimal.ONE;
            Payment payment = Payment.builder()
                    .provider(PaymentProvider.NONE)
                    .subscription(subscription)
                    .amount(amountToPay)
                    .currency(user.getCurrency())
                    .status(PaymentStatus.COMPLETED)
                    .build();

            Subscription current = subscriptionService.findCurrentActiveSubscription(user);
            if (current != null) {
                current.setStatus(SubscriptionStatus.EXPIRED);
                current.setActive(false);
                subscriptionRepository.save(current);
            } else {
                log.info("No current active subscription found for user: {}", user.getUsername());
            }

            // Subtract credit balance
            log.info("User {} has sufficient credit balance: {}, deducting amount: {}",
                    user.getUsername(), user.getCreditBalance(), amountToPay.doubleValue());
            double newCreditBalance = user.getCreditBalance() + costResponse.getProRateValue().doubleValue() - amountToPay.doubleValue();
            if (newCreditBalance < 0) {
                newCreditBalance = 0;
            }
            log.info("New credit balance for user {}: {}", user.getUsername(), newCreditBalance);
            user.setCreditBalance(newCreditBalance);
            userRepository.save(user);

            paymentService.save(payment);

            subscription.setStatus(SubscriptionStatus.ACTIVE);
            subscription.setActive(true);
            subscriptionRepository.save(subscription);

            return ResponseEntity.ok(Map.of(
                    "subscriptionId", subscription.getId(),
                    "paymentUrl", ""
            ));
        } else {
            String paymentUrl = paymentService.createVnpayPayment(subscription, amountToPay, user.getCurrency(), new HashMap<>());
            return ResponseEntity.ok(Map.of(
                    "subscriptionId", subscription.getId(),
                    "paymentUrl", paymentUrl
            ));
        }
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

    @GetMapping("/cost")
    public ResponseEntity<?> getSubscriptionCost(
            @RequestParam Long subscriptionPlanId,
            @RequestParam(required = false, defaultValue = "false") boolean applyImmediately,
            @AuthenticationPrincipal UserDetails userDetails) {

        User user = userService.getCurrentUser(userDetails);
        log.info("Calculating subscription cost for user: {}, planId: {}, applyImmediately: {}",
                user.getUsername(), subscriptionPlanId, applyImmediately);
        SubscriptionCostResponse costResponse = subscriptionService.calculateSubscriptionCost(user, subscriptionPlanId,
                applyImmediately);
        log.info("Calculated cost response: {}", costResponse);
        return ResponseEntity.ok(costResponse);
    }
}
