package intelink.repositories;

import intelink.models.Subscription;
import intelink.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    List<Subscription> findByUser(User user);

    @Query("SELECT ps FROM Subscription ps WHERE ps.user = ?1 AND ps.active = true AND (ps.expiresAt IS NULL OR ps.expiresAt > ?2)")
    Optional<Subscription> findActiveSubscriptionByUser(User user, Instant now);

    @Query("SELECT ps FROM Subscription ps WHERE ps.active = true AND ps.expiresAt < ?1")
    List<Subscription> findExpiredActiveSubscriptions(Instant now);

    Optional<Subscription> findById(UUID subscriptionId);

}