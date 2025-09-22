package intelink.controllers;

import intelink.dto.request.subscription.CreateSubscriptionRequest;
import intelink.dto.response.subscription.CancelSubscriptionResponse;
import intelink.dto.response.subscription.GetAllSubscriptionsResponse;
import intelink.dto.response.subscription.SubscriptionResponse;
import intelink.models.Subscription;
import intelink.models.User;
import intelink.services.interfaces.ISubscriptionService;
import intelink.services.interfaces.IUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/subscription")
@RequiredArgsConstructor
@Slf4j
public class SubscriptionController {

    private final ISubscriptionService subscriptionService;
    private final IUserService userService;

    @GetMapping
    public ResponseEntity<?> getAll(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails);
        List<Subscription> subscriptions = subscriptionService.findByUser(user);
        return ResponseEntity.ok(GetAllSubscriptionsResponse.fromEntities(subscriptions));
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentSubscription(@AuthenticationPrincipal UserDetails userDetails) {
        User user = userService.getCurrentUser(userDetails);
        Subscription subscription = subscriptionService.findCurrentActiveSubscription(user);
        return ResponseEntity.ok(SubscriptionResponse.fromEntity(subscription));
    }

    @PostMapping
    public ResponseEntity<?> registerSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getCurrentUser(userDetails);
        Subscription subscription = subscriptionService.registerSubscription(user, request);
        return ResponseEntity.ok(SubscriptionResponse.fromEntity(subscription));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<?> cancelSubscription(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = userService.getCurrentUser(userDetails);
        subscriptionService.cancelSubscription(user, id);

        return ResponseEntity.ok(CancelSubscriptionResponse.builder()
                .success(true)
                .message("Subscription cancelled successfully")
                .subscriptionId(id)
                .build());
    }
}
