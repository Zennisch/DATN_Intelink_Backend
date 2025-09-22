package intelink.services;

import intelink.dto.request.subscription.RegisterSubscriptionRequest;
import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;
import intelink.models.enums.SubscriptionStatus;
import intelink.repositories.SubscriptionPlanRepository;
import intelink.repositories.SubscriptionRepository;
import intelink.services.interfaces.ISubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Override
    public List<Subscription> findByUser(User user) {
        return subscriptionRepository.findByUser(user);
    }

    @Override
    public Subscription findCurrentActiveSubscription(User user) {
        return subscriptionRepository.findActiveSubscriptionByUser(user, Instant.now())
                .orElse(null);
    }

    @Override
    @Transactional
    public Subscription registerSubscription(User user, RegisterSubscriptionRequest request) {
        // Find subscription plan
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getSubscriptionPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found with ID: " + request.getSubscriptionPlanId()));

        // Deactivate current active subscription if exists
        subscriptionRepository.findActiveSubscriptionByUser(user, Instant.now())
                .ifPresent(activeSubscription -> {
                    log.info("Deactivating current subscription {} for user {}", activeSubscription.getId(), user.getId());
                    activeSubscription.setActive(false);
                    subscriptionRepository.save(activeSubscription);
                });

        // Calculate expiry date
        Instant expiresAt = null;
        if (plan.getType().name().equals("FREE")) {
            // Free plan never expires
            expiresAt = null;
        } else {
            // Paid plans expire after billing interval
            switch (plan.getBillingInterval()) {
                case MONTHLY -> expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
                case YEARLY -> expiresAt = Instant.now().plus(365, ChronoUnit.DAYS);
                default -> throw new RuntimeException("Unsupported billing interval: " + plan.getBillingInterval());
            }
        }

        // Create new subscription
        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionPlan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .active(true)
                .startsAt(Instant.now())
                .expiresAt(expiresAt)
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);
        log.info("Created new subscription {} for user {} with plan {}", savedSubscription.getId(), user.getId(), plan.getType());

        return savedSubscription;
    }

    @Override
    @Transactional
    public void cancelSubscription(User user, UUID subscriptionId) {
        Subscription subscription = subscriptionRepository.findById(subscriptionId)
                .orElseThrow(() -> new RuntimeException("Subscription not found with ID: " + subscriptionId));

        // Verify subscription belongs to user
        if (!subscription.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Subscription does not belong to user");
        }

        // Check if subscription can be cancelled (not started or not active)
        Instant now = Instant.now();
        if (subscription.getStatus() == SubscriptionStatus.ACTIVE && subscription.getStartsAt().isBefore(now)) {
            throw new RuntimeException("Cannot cancel active subscription that has already started");
        }

        // Cancel subscription
        subscription.setStatus(SubscriptionStatus.CANCELED);
        subscription.setActive(false);
        subscriptionRepository.save(subscription);

        log.info("Cancelled subscription {} for user {}", subscriptionId, user.getId());
    }
}
