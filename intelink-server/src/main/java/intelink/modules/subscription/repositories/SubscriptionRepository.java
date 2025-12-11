package intelink.modules.subscription.repositories;

import intelink.models.Subscription;
import intelink.models.User;
import intelink.models.enums.SubscriptionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {

    @Query("SELECT s FROM Subscription s WHERE s.user = :user AND s.active = true AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveSubscriptionByUser(@Param("user") User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s WHERE s.user = :user AND s.active = true AND s.status = 'ACTIVE'")
    Optional<Subscription> findActiveSubscriptionByUserWithLock(@Param("user") User user);

    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.subscriptionPlan WHERE s.id = :id")
    Optional<Subscription> findByIdWithPlan(@Param("id") UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM Subscription s LEFT JOIN FETCH s.subscriptionPlan WHERE s.id = :id")
    Optional<Subscription> findByIdWithPlanAndLock(@Param("id") UUID id);

    List<Subscription> findByUserOrderByCreatedAtDesc(User user);

    @Query("SELECT s FROM Subscription s WHERE s.status = :status AND s.expiresAt < :expiryThreshold")
    List<Subscription> findExpiredSubscriptions(@Param("status") SubscriptionStatus status, @Param("expiryThreshold") Instant expiryThreshold);

    @Query("SELECT s FROM Subscription s WHERE s.status = 'PENDING' AND s.createdAt < :threshold")
    List<Subscription> findPendingSubscriptionsOlderThan(@Param("threshold") Instant threshold);
}
