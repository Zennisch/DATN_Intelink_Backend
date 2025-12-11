package intelink.modules.subscription.services;

import intelink.dto.subscription.SubscriptionPlanRequest;
import intelink.models.SubscriptionPlan;
import intelink.modules.subscription.repositories.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanService {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getAllPlans() {
        return subscriptionPlanRepository.findAllByOrderByPriceAsc();
    }

    @Transactional(readOnly = true)
    public SubscriptionPlan getPlanById(Long id) {
        return subscriptionPlanRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found with id: " + id));
    }

    @Transactional
    public SubscriptionPlan createPlan(SubscriptionPlanRequest request) {
        // Check if plan type already exists
        if (subscriptionPlanRepository.existsByType(request.type())) {
            throw new IllegalArgumentException("Subscription plan with type " + request.type() + " already exists");
        }

        SubscriptionPlan plan = SubscriptionPlan.builder()
                .type(request.type())
                .billingInterval(request.billingInterval())
                .active(true)
                .description(request.description())
                .price(request.price())
                .maxShortUrls(request.maxShortUrls())
                .maxUsagePerUrl(request.maxUsagePerUrl())
                .shortCodeCustomizationEnabled(request.shortCodeCustomizationEnabled())
                .statisticsEnabled(request.statisticsEnabled())
                .apiAccessEnabled(request.apiAccessEnabled())
                .build();

        return subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public SubscriptionPlan updatePlan(Long id, SubscriptionPlanRequest request) {
        SubscriptionPlan plan = getPlanById(id);

        // Check if changing to a type that already exists
        if (!plan.getType().equals(request.type()) && 
            subscriptionPlanRepository.existsByType(request.type())) {
            throw new IllegalArgumentException("Subscription plan with type " + request.type() + " already exists");
        }

        plan.setType(request.type());
        plan.setBillingInterval(request.billingInterval());
        plan.setDescription(request.description());
        plan.setPrice(request.price());
        plan.setMaxShortUrls(request.maxShortUrls());
        plan.setMaxUsagePerUrl(request.maxUsagePerUrl());
        plan.setShortCodeCustomizationEnabled(request.shortCodeCustomizationEnabled());
        plan.setStatisticsEnabled(request.statisticsEnabled());
        plan.setApiAccessEnabled(request.apiAccessEnabled());

        return subscriptionPlanRepository.save(plan);
    }

    @Transactional
    public void deletePlan(Long id) {
        SubscriptionPlan plan = getPlanById(id);
        subscriptionPlanRepository.delete(plan);
        log.info("[SubscriptionPlanService.deletePlan] Deleted subscription plan with id: {}", id);
    }

    @Transactional
    public SubscriptionPlan togglePlanStatus(Long id) {
        SubscriptionPlan plan = getPlanById(id);
        plan.setActive(!plan.getActive());
        return subscriptionPlanRepository.save(plan);
    }
}
