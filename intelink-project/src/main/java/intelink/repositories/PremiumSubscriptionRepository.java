package intelink.repositories;

import intelink.models.PremiumSubscription;
import intelink.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PremiumSubscriptionRepository extends JpaRepository<PremiumSubscription, UUID> {
    
    List<PremiumSubscription> findByUser(User user);
    
    @Query("SELECT ps FROM PremiumSubscription ps WHERE ps.user = ?1 AND ps.active = true AND ps.expiresAt > ?2")
    Optional<PremiumSubscription> findActiveSubscriptionByUser(User user, Instant now);
    
    @Query("SELECT ps FROM PremiumSubscription ps WHERE ps.active = true AND ps.expiresAt < ?1")
    List<PremiumSubscription> findExpiredActiveSubscriptions(Instant now);
    
}
