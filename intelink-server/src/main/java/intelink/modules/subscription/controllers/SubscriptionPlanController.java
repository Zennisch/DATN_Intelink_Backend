package intelink.modules.subscription.controllers;

import intelink.dto.subscription.SubscriptionPlanRequest;
import intelink.dto.subscription.SubscriptionPlanResponse;
import intelink.models.SubscriptionPlan;
import intelink.modules.subscription.services.SubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/subscription-plans")
public class SubscriptionPlanController {

    private final SubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public ResponseEntity<?> getAllPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanService.getAllPlans();
        List<SubscriptionPlanResponse> response = plans.stream()
                .map(SubscriptionPlanResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getPlanById(@PathVariable Long id) {
        SubscriptionPlan plan = subscriptionPlanService.getPlanById(id);
        SubscriptionPlanResponse response = SubscriptionPlanResponse.fromEntity(plan);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<?> createPlan(@Valid @RequestBody SubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanService.createPlan(request);
        SubscriptionPlanResponse response = SubscriptionPlanResponse.fromEntity(plan);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updatePlan(
            @PathVariable Long id,
            @Valid @RequestBody SubscriptionPlanRequest request
    ) {
        SubscriptionPlan plan = subscriptionPlanService.updatePlan(id, request);
        SubscriptionPlanResponse response = SubscriptionPlanResponse.fromEntity(plan);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deletePlan(@PathVariable Long id) {
        subscriptionPlanService.deletePlan(id);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/toggle-status")
    public ResponseEntity<?> togglePlanStatus(@PathVariable Long id) {
        SubscriptionPlan plan = subscriptionPlanService.togglePlanStatus(id);
        SubscriptionPlanResponse response = SubscriptionPlanResponse.fromEntity(plan);
        return ResponseEntity.ok(response);
    }
}
