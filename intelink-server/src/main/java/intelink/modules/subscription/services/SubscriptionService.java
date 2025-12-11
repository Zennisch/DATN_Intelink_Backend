package intelink.modules.subscription.services;

import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;
import intelink.models.enums.SubscriptionStatus;
import intelink.modules.subscription.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanService subscriptionPlanService;

    @Transactional(readOnly = true)
    public Optional<Subscription> getActiveSubscriptionByUser(User user) {
        return subscriptionRepository.findActiveSubscriptionByUser(user);
    }

    @Transactional(readOnly = true)
    public Optional<Subscription> findById(UUID id) {
        return subscriptionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Optional<Subscription> findByIdWithPlan(UUID id) {
        return subscriptionRepository.findByIdWithPlan(id);
    }

    @Transactional(readOnly = true)
    public List<Subscription> getSubscriptionsByUser(User user) {
        return subscriptionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    /**
     * Create a new subscription in PENDING status
     */
    @Transactional
    public Subscription createPendingSubscription(User user, Long planId) {
        SubscriptionPlan plan = subscriptionPlanService.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription plan not found with id: " + planId));

        if (!plan.getActive()) {
            throw new IllegalArgumentException("Subscription plan is not active");
        }

        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionPlan(plan)
                .status(SubscriptionStatus.PENDING)
                .active(false)
                .creditUsed(0.0)
                .build();

        return subscriptionRepository.save(subscription);
    }

    /**
     * Activate a subscription after successful payment with proper locking
     * This method handles:
     * - Deactivating current active subscription
     * - Calculating pro-rate credit for remaining time
     * - Activating new subscription with credit applied
     */
    @Transactional
    public Subscription activateSubscription(UUID subscriptionId, User user) {
        // Lock the new subscription
        Subscription newSubscription = subscriptionRepository.findByIdWithPlanAndLock(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + subscriptionId));

        if (!newSubscription.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Subscription does not belong to user");
        }

        if (newSubscription.getStatus() != SubscriptionStatus.PENDING) {
            throw new IllegalStateException("Subscription is not in PENDING status");
        }

        // Lock and get current active subscription
        Optional<Subscription> currentActiveOpt = subscriptionRepository.findActiveSubscriptionByUserWithLock(user);

        Instant now = Instant.now();
        double proratedCredit = 0.0;

        if (currentActiveOpt.isPresent()) {
            Subscription currentSubscription = currentActiveOpt.get();
            
            // Calculate pro-rate credit only if current subscription hasn't expired
            if (currentSubscription.getExpiresAt() != null && currentSubscription.getExpiresAt().isAfter(now)) {
                proratedCredit = calculateProrateCredit(currentSubscription, now);
                log.info("[SubscriptionService.activateSubscription] Calculated pro-rate credit: {} for subscription {}", 
                        proratedCredit, currentSubscription.getId());
            }

            // Deactivate current subscription
            currentSubscription.setActive(false);
            currentSubscription.setStatus(SubscriptionStatus.EXPIRED);
            subscriptionRepository.save(currentSubscription);
            
            log.info("[SubscriptionService.activateSubscription] Deactivated current subscription: {}", 
                    currentSubscription.getId());
        }

        // Activate new subscription
        newSubscription.setStatus(SubscriptionStatus.ACTIVE);
        newSubscription.setActive(true);
        newSubscription.setActivatedAt(now);
        
        // Calculate expiration based on plan duration
        SubscriptionPlan plan = newSubscription.getSubscriptionPlan();
        Instant expiresAt = calculateExpirationDate(now, plan.getDuration());
        newSubscription.setExpiresAt(expiresAt);
        
        // Store pro-rated credit
        if (proratedCredit > 0) {
            newSubscription.setProratedValue(proratedCredit);
        }

        Subscription activated = subscriptionRepository.save(newSubscription);
        
        log.info("[SubscriptionService.activateSubscription] Activated subscription: {}, expires at: {}, prorated credit: {}", 
                activated.getId(), expiresAt, proratedCredit);

        return activated;
    }

    /**
     * Calculate pro-rate credit for remaining time on current subscription
     * Formula: (Total Plan Price / Total Days) * Remaining Days
     */
    private double calculateProrateCredit(Subscription currentSubscription, Instant currentTime) {
        if (currentSubscription.getActivatedAt() == null || currentSubscription.getExpiresAt() == null) {
            return 0.0;
        }

        Instant activatedAt = currentSubscription.getActivatedAt();
        Instant expiresAt = currentSubscription.getExpiresAt();

        // Prevent calculation if already expired or invalid dates
        if (expiresAt.isBefore(currentTime) || expiresAt.isBefore(activatedAt)) {
            return 0.0;
        }

        long totalDays = ChronoUnit.DAYS.between(activatedAt, expiresAt);
        long remainingDays = ChronoUnit.DAYS.between(currentTime, expiresAt);

        // Prevent division by zero
        if (totalDays <= 0) {
            return 0.0;
        }

        double planPrice = currentSubscription.getSubscriptionPlan().getPrice();
        double dailyValue = planPrice / totalDays;
        double credit = dailyValue * remainingDays;

        // Round to 2 decimal places
        return Math.round(credit * 100.0) / 100.0;
    }

    /**
     * Calculate cost for upgrading/downgrading to a new plan (public method for preview)
     */
    @Transactional(readOnly = true)
    public double calculateProratedCreditForUser(User user) {
        Optional<Subscription> currentActiveOpt = subscriptionRepository.findActiveSubscriptionByUser(user);
        
        if (currentActiveOpt.isEmpty()) {
            return 0.0;
        }
        
        return calculateProrateCredit(currentActiveOpt.get(), Instant.now());
    }

    /**
     * Calculate expiration date based on duration in days
     */
    private Instant calculateExpirationDate(Instant startDate, Integer durationDays) {
        if (durationDays == null || durationDays <= 0) {
            // For lifetime plans, set expiration to 100 years from now
            return startDate.plus(Duration.ofDays(36500));
        }
        return startDate.plus(Duration.ofDays(durationDays));
    }

    /**
     * Cancel an active subscription
     */
    @Transactional
    public Subscription cancelSubscription(UUID subscriptionId, User user) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new IllegalArgumentException("Subscription not found with id: " + subscriptionId));

        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Subscription does not belong to user");
        }

        if (subscription.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new IllegalStateException("Only active subscriptions can be canceled");
        }

        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setActive(false);

        return subscriptionRepository.save(subscription);
    }

    /**
     * Clean up expired subscriptions (scheduled task)
     */
    @Transactional
    public int cleanupExpiredSubscriptions() {
        List<Subscription> expiredSubscriptions = subscriptionRepository
                .findExpiredSubscriptions(SubscriptionStatus.ACTIVE, Instant.now());

        int count = 0;
        for (Subscription subscription : expiredSubscriptions) {
            subscription.setStatus(SubscriptionStatus.EXPIRED);
            subscription.setActive(false);
            subscriptionRepository.save(subscription);
            count++;
        }

        log.info("[SubscriptionService.cleanupExpiredSubscriptions] Cleaned up {} expired subscriptions", count);
        return count;
    }

    /**
     * Clean up old pending subscriptions (scheduled task)
     */
    @Transactional
    public int cleanupOldPendingSubscriptions() {
        Instant threshold = Instant.now().minus(Duration.ofHours(24));
        List<Subscription> oldPending = subscriptionRepository.findPendingSubscriptionsOlderThan(threshold);

        int count = 0;
        for (Subscription subscription : oldPending) {
            subscription.setStatus(SubscriptionStatus.CANCELED);
            subscriptionRepository.save(subscription);
            count++;
        }

        log.info("[SubscriptionService.cleanupOldPendingSubscriptions] Cleaned up {} old pending subscriptions", count);
        return count;
    }
}
