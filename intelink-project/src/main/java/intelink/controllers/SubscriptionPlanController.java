package intelink.controllers;


import intelink.dto.response.subscription.SubscriptionPlanResponse;
import intelink.services.interfaces.ISubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/plan")
@RequiredArgsConstructor
public class SubscriptionPlanController {
    private final ISubscriptionPlanService subscriptionPlanService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAll() {
        return ResponseEntity.ok(subscriptionPlanService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResponse> getById(@PathVariable Long id) {
        return subscriptionPlanService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<SubscriptionPlanResponse> create(@RequestBody SubscriptionPlanResponse dto) {
        return ResponseEntity.ok(subscriptionPlanService.save(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubscriptionPlanResponse> update(@PathVariable Long id, @RequestBody SubscriptionPlanResponse dto) {
        return subscriptionPlanService.update(id, dto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (subscriptionPlanService.deleteById(id)) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.notFound().build();
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<SubscriptionPlanResponse> toggleStatus(@PathVariable Long id) {
        return subscriptionPlanService.toggleStatus(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}