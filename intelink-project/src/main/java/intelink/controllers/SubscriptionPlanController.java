package intelink.controllers;

import intelink.dto.request.subscription.CreateSubscriptionPlanRequest;
import intelink.dto.request.subscription.UpdateSubscriptionPlanRequest;
import intelink.dto.response.subscription.DeleteSubscriptionPlanResponse;
import intelink.dto.response.subscription.GetAllSubscriptionPlansResponse;
import intelink.dto.response.subscription.SubscriptionPlanResponse;
import intelink.models.SubscriptionPlan;
import intelink.services.interfaces.ISubscriptionPlanService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/plan")
@RequiredArgsConstructor
public class SubscriptionPlanController {
    private final ISubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public ResponseEntity<?> getAll() {
        List<SubscriptionPlan> plans = subscriptionPlanService.findAll();
        return ResponseEntity.ok(GetAllSubscriptionPlansResponse.fromEntities(plans));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        SubscriptionPlan plan = subscriptionPlanService.findById(id);
        return ResponseEntity.ok(SubscriptionPlanResponse.fromEntity(plan));
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanService.save(request);
        return ResponseEntity.ok(SubscriptionPlanResponse.fromEntity(plan));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UpdateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = subscriptionPlanService.update(id, request);
        return ResponseEntity.ok(SubscriptionPlanResponse.fromEntity(plan));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        subscriptionPlanService.deleteById(id);
        return ResponseEntity.ok(DeleteSubscriptionPlanResponse.builder()
                .success(true)
                .message("Subscription plan deleted successfully")
                .build());
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggleStatus(@PathVariable Long id) {
        SubscriptionPlan plan = subscriptionPlanService.toggleStatus(id);
        return ResponseEntity.ok(SubscriptionPlanResponse.fromEntity(plan));
    }
}