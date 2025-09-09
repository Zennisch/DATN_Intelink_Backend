package intelink.services;

import intelink.dto.request.subscription.CreateSubscriptionPlanRequest;
import intelink.dto.request.subscription.UpdateSubscriptionPlanRequest;
import intelink.models.SubscriptionPlan;
import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import intelink.repositories.SubscriptionPlanRepository;
import intelink.services.interfaces.ISubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService implements ISubscriptionPlanService {
    private final SubscriptionPlanRepository repository;

    @Override
    public List<SubscriptionPlan> findAll() {
        return repository.findAll();
    }

    public SubscriptionPlan findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with ID: " + id));
    }

    public SubscriptionPlan save(CreateSubscriptionPlanRequest request) {
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .type(SubscriptionPlanType.fromString(request.getType()))
                .description(request.getDescription())
                .price(request.getPrice())
                .billingInterval(SubscriptionPlanBillingInterval.fromString(request.getBillingInterval()))
                .maxShortUrls(request.getMaxShortUrls())
                .shortCodeCustomizationEnabled(request.getShortCodeCustomizationEnabled())
                .statisticsEnabled(request.getStatisticsEnabled())
                .customDomainEnabled(request.getCustomDomainEnabled())
                .apiAccessEnabled(request.getApiAccessEnabled())
                .active(request.getActive())
                .max_usage_per_url(request.getMaxUsagePerUrl())
                .build();
        return repository.save(plan);
    }

    public SubscriptionPlan update(Long id, UpdateSubscriptionPlanRequest request) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Subscription plan not found with ID: " + id);
        }
        
        SubscriptionPlan plan = SubscriptionPlan.builder()
                .id(id)
                .type(SubscriptionPlanType.fromString(request.getType()))
                .description(request.getDescription())
                .price(request.getPrice())
                .billingInterval(SubscriptionPlanBillingInterval.fromString(request.getBillingInterval()))
                .maxShortUrls(request.getMaxShortUrls())
                .shortCodeCustomizationEnabled(request.getShortCodeCustomizationEnabled())
                .statisticsEnabled(request.getStatisticsEnabled())
                .customDomainEnabled(request.getCustomDomainEnabled())
                .apiAccessEnabled(request.getApiAccessEnabled())
                .active(request.getActive())
                .max_usage_per_url(request.getMaxUsagePerUrl())
                .build();
        return repository.save(plan);
    }

    public void deleteById(Long id) {
        if (!repository.existsById(id)) {
            throw new RuntimeException("Subscription plan not found with ID: " + id);
        }
        repository.deleteById(id);
    }

    public SubscriptionPlan toggleStatus(Long id) {
        SubscriptionPlan plan = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with ID: " + id));
        plan.setActive(!plan.getActive());
        return repository.save(plan);
    }
}