package intelink.utils.seeder;

import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;
import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import intelink.models.enums.SubscriptionStatus;
import intelink.modules.auth.repositories.UserRepository;
import intelink.modules.subscription.repositories.SubscriptionPlanRepository;
import intelink.modules.subscription.repositories.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionSeeder {

    private final SubscriptionRepository subscriptionRepository;
    private final UserRepository userRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional
    public void seed() {
        if (subscriptionRepository.count() == 0) {
            SubscriptionPlan freePlan = subscriptionPlanRepository.findByTypeAndBillingInterval(
                    SubscriptionPlanType.FREE, SubscriptionPlanBillingInterval.NONE)
                    .orElseThrow(() -> new RuntimeException("Free plan not found"));

            List<User> users = userRepository.findAll();
            List<Subscription> subscriptions = new ArrayList<>();

            for (User user : users) {
                Subscription subscription = Subscription.builder()
                        .user(user)
                        .subscriptionPlan(freePlan)
                        .status(SubscriptionStatus.ACTIVE)
                        .active(true)
                        .creditUsed(0.0)
                        .activatedAt(Instant.now())
                        .expiresAt(null) // Lifetime
                        .build();
                subscriptions.add(subscription);
            }

            subscriptionRepository.saveAll(subscriptions);
        }
    }
}
