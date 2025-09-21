package intelink.services.interfaces;

import intelink.dto.request.subscription.CreateSubscriptionPlanRequest;
import intelink.dto.request.subscription.UpdateSubscriptionPlanRequest;
import intelink.models.SubscriptionPlan;

import java.util.List;

public interface ISubscriptionPlanService {
    List<SubscriptionPlan> findAll();

    SubscriptionPlan findById(Long id);

    SubscriptionPlan save(CreateSubscriptionPlanRequest request);

    SubscriptionPlan update(Long id, UpdateSubscriptionPlanRequest request);

    void deleteById(Long id);

    SubscriptionPlan toggleStatus(Long id);
}