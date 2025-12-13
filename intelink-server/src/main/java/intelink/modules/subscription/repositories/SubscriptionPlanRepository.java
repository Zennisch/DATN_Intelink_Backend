package intelink.modules.subscription.repositories;

import intelink.models.SubscriptionPlan;
import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    List<SubscriptionPlan> findByType(SubscriptionPlanType type);

    Optional<SubscriptionPlan> findByTypeAndBillingInterval(SubscriptionPlanType type, SubscriptionPlanBillingInterval billingInterval);
    
    List<SubscriptionPlan> findAllByOrderByPriceAsc();
    
    boolean existsByType(SubscriptionPlanType type);
}
