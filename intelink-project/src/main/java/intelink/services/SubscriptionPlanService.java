package intelink.services;


import intelink.dto.response.subscription.SubscriptionPlanResponse;
import intelink.models.SubscriptionPlan;
import intelink.repositories.SubscriptionPlanRepository;
import intelink.services.interfaces.ISubscriptionPlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SubscriptionPlanService implements ISubscriptionPlanService {
    private final SubscriptionPlanRepository repository;

    @Override
    public Map<String, Object> findAll() {
        List<SubscriptionPlanResponse> plans = repository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
        Map<String, Object> result = new HashMap<>();
        result.put("data", plans);
        return result;
    }

    public Optional<SubscriptionPlanResponse> findById(Long id) {
        return repository.findById(id).map(this::toDto);
    }

    public SubscriptionPlanResponse save(SubscriptionPlanResponse dto) {
        SubscriptionPlan plan = toEntity(dto);
        SubscriptionPlan saved = repository.save(plan);
        return toDto(saved);
    }

    public Optional<SubscriptionPlanResponse> update(Long id, SubscriptionPlanResponse dto) {
        return repository.findById(id).map(existing -> {
            SubscriptionPlan plan = toEntity(dto);
            plan.setId(id);
            SubscriptionPlan updated = repository.save(plan);
            return toDto(updated);
        });
    }

    public boolean deleteById(Long id) {
        if (repository.existsById(id)) {
            repository.deleteById(id);
            return true;
        }
        return false;
    }

    // Chuyển đổi giữa entity và dto
    private SubscriptionPlanResponse toDto(SubscriptionPlan plan) {
        return SubscriptionPlanResponse.builder()
                .id(plan.getId())
                .type(plan.getType())
                .description(plan.getDescription())
                .price(plan.getPrice())
                .billingInterval(plan.getBillingInterval())
                .maxShortUrls(plan.getMaxShortUrls())
                .shortCodeCustomizationEnabled(plan.getShortCodeCustomizationEnabled())
                .statisticsEnabled(plan.getStatisticsEnabled())
                .customDomainEnabled(plan.getCustomDomainEnabled())
                .apiAccessEnabled(plan.getApiAccessEnabled())
                .active(plan.getActive())
                .createdAt(plan.getCreatedAt())
                .maxUsagePerUrl(plan.getMax_usage_per_url())
                .build();
    }

    private SubscriptionPlan toEntity(SubscriptionPlanResponse dto) {
        return SubscriptionPlan.builder()
                .id(dto.getId())
                .type(dto.getType())
                .description(dto.getDescription())
                .price(dto.getPrice())
                .billingInterval(dto.getBillingInterval())
                .maxShortUrls(dto.getMaxShortUrls())
                .shortCodeCustomizationEnabled(dto.getShortCodeCustomizationEnabled())
                .statisticsEnabled(dto.getStatisticsEnabled())
                .customDomainEnabled(dto.getCustomDomainEnabled())
                .apiAccessEnabled(dto.getApiAccessEnabled())
                .active(dto.getActive())
                .createdAt(dto.getCreatedAt())
                .max_usage_per_url(dto.getMaxUsagePerUrl())
                .build();
    }


}