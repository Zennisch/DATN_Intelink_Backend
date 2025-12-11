package intelink.modules.subscription.repositories;

import intelink.models.SubscriptionPlan;
import intelink.models.enums.SubscriptionPlanType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    Optional<SubscriptionPlan> findByType(SubscriptionPlanType type);
    
    List<SubscriptionPlan> findAllByOrderByPriceAsc();
    
    boolean existsByType(SubscriptionPlanType type);
}
