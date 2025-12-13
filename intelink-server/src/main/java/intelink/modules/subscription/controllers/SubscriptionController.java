package intelink.modules.subscription.controllers;

import intelink.dto.subscription.CalculateCostRequest;
import intelink.dto.subscription.CalculateCostResponse;
import intelink.dto.subscription.CreateSubscriptionRequest;
import intelink.dto.subscription.SubscriptionResponse;
import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;
import intelink.modules.auth.services.AuthService;
import intelink.modules.subscription.services.SubscriptionPlanService;
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

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/subscriptions")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Subscription", description = "Subscription management endpoints")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final SubscriptionPlanService subscriptionPlanService;
    private final AuthService authService;

    @GetMapping("/active")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get active subscription", description = "Get current user's active subscription")
    public ResponseEntity<SubscriptionResponse> getActiveSubscription() {
        User currentUser = authService.getCurrentUser();
        
        Optional<Subscription> subscription = subscriptionService.getActiveSubscriptionByUser(currentUser);
        
        return subscription
                .map(sub -> ResponseEntity.ok(SubscriptionResponse.fromEntity(sub)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get all subscriptions", description = "Get all subscriptions of current user")
    public ResponseEntity<List<SubscriptionResponse>> getAllSubscriptions() {
        User currentUser = authService.getCurrentUser();
        
        List<Subscription> subscriptions = subscriptionService.getSubscriptionsByUser(currentUser);
        List<SubscriptionResponse> response = subscriptions.stream()
                .map(SubscriptionResponse::fromEntity)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Get subscription by ID", description = "Get subscription details by ID")
    public ResponseEntity<SubscriptionResponse> getSubscriptionById(@PathVariable UUID id) {
        User currentUser = authService.getCurrentUser();
        
        Optional<Subscription> subscription = subscriptionService.findByIdWithPlan(id);
        
        if (subscription.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        if (!subscription.get().getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        
        return ResponseEntity.ok(SubscriptionResponse.fromEntity(subscription.get()));
    }

    @PostMapping
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Create subscription", description = "Create a new pending subscription")
    public ResponseEntity<SubscriptionResponse> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {
        
        User currentUser = authService.getCurrentUser();
        
        try {
            Subscription subscription = subscriptionService.createPendingSubscription(
                    currentUser, 
                    request.planId());
            
            log.info("[SubscriptionController.createSubscription] Created pending subscription {} for user {}", 
                    subscription.getId(), currentUser.getId());
            
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(SubscriptionResponse.fromEntity(subscription));
                    
        } catch (IllegalArgumentException e) {
            log.error("[SubscriptionController.createSubscription] Error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Cancel subscription", description = "Cancel an active subscription")
    public ResponseEntity<SubscriptionResponse> cancelSubscription(@PathVariable UUID id) {
        User currentUser = authService.getCurrentUser();
        
        try {
            Subscription subscription = subscriptionService.cancelSubscription(id, currentUser);
            return ResponseEntity.ok(SubscriptionResponse.fromEntity(subscription));
            
        } catch (IllegalArgumentException e) {
            log.error("[SubscriptionController.cancelSubscription] Error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
            
        } catch (IllegalStateException e) {
            log.error("[SubscriptionController.cancelSubscription] State error: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PostMapping("/calculate-cost")
    @SecurityRequirement(name = "Bearer Authentication")
    @Operation(summary = "Calculate subscription cost", description = "Calculate final cost with pro-rated credit before purchasing")
    public ResponseEntity<CalculateCostResponse> calculateCost(
            @Valid @RequestBody CalculateCostRequest request) {
        
        User currentUser = authService.getCurrentUser();
        
        try {
            // Get new plan
            SubscriptionPlan newPlan = subscriptionPlanService.getPlanById(request.planId());
            
            // Get current active subscription
            Optional<Subscription> currentSubscriptionOpt = subscriptionService.getActiveSubscriptionByUser(currentUser);
            
            // Calculate pro-rated credit
            double proratedCredit = subscriptionService.calculateProratedCreditForUser(currentUser);
            
            // Calculate final cost
            double finalCost = Math.max(0, newPlan.getPrice() - proratedCredit);
            double savings = proratedCredit;
            
            CalculateCostResponse.CalculateCostResponseBuilder responseBuilder = CalculateCostResponse.builder()
                    .planId(newPlan.getId())
                    .planType(newPlan.getType().name())
                    .planPrice(newPlan.getPrice())
                    .durationDays(newPlan.getDuration())
                    .proratedCredit(proratedCredit)
                    .finalCost(finalCost)
                    .savings(savings);
            
            if (currentSubscriptionOpt.isPresent()) {
                Subscription currentSub = currentSubscriptionOpt.get();
                responseBuilder
                        .currentPlanType(currentSub.getSubscriptionPlan().getType().name())
                        .currentExpiresAt(currentSub.getExpiresAt())
                        .message("You have an active " + currentSub.getSubscriptionPlan().getType().name() + 
                                " subscription. Pro-rated credit will be applied.");
            } else {
                responseBuilder.message("No active subscription. Full price will be charged.");
            }
            
            return ResponseEntity.ok(responseBuilder.build());
            
        } catch (IllegalArgumentException e) {
            log.error("[SubscriptionController.calculateCost] Error: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
