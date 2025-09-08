package intelink.services.interfaces;



import intelink.dto.response.subscription.SubscriptionPlanResponse;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ISubscriptionPlanService {
    Map<String, Object> findAll();
    Optional<SubscriptionPlanResponse> findById(Long id);
    SubscriptionPlanResponse save(SubscriptionPlanResponse dto);
    Optional<SubscriptionPlanResponse> update(Long id, SubscriptionPlanResponse dto);
    boolean deleteById(Long id);
}