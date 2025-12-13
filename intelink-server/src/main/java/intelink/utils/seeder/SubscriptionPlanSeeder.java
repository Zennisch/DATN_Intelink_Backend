package intelink.utils.seeder;

import intelink.models.SubscriptionPlan;
import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import intelink.modules.subscription.repositories.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public class SubscriptionPlanSeeder {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    @Transactional
    public void seed() {
        if (subscriptionPlanRepository.count() == 0) {
            List<SubscriptionPlan> plans = Arrays.asList(
                // Free Plan (Lifetime)
                SubscriptionPlan.builder()
                    .type(SubscriptionPlanType.FREE)
                    .billingInterval(SubscriptionPlanBillingInterval.NONE)
                    .price(0.0)
                    .maxShortUrls(10)
                    .maxUsagePerUrl(100)
                    .description("Free plan with basic features")
                    .active(true)
                    .build(),

                // Monthly Plans
                SubscriptionPlan.builder()
                    .type(SubscriptionPlanType.PRO)
                    .billingInterval(SubscriptionPlanBillingInterval.MONTHLY)
                    .price(9.99)
                    .maxShortUrls(100)
                    .maxUsagePerUrl(1000)
                    .description("Pro plan for power users")
                    .active(true)
                    .build(),
                SubscriptionPlan.builder()
                    .type(SubscriptionPlanType.ENTERPRISE)
                    .billingInterval(SubscriptionPlanBillingInterval.MONTHLY)
                    .price(99.99)
                    .maxShortUrls(1000)
                    .maxUsagePerUrl(10000)
                    .description("Enterprise plan for businesses")
                    .active(true)
                    .build(),

                // Yearly Plans
                SubscriptionPlan.builder()
                    .type(SubscriptionPlanType.PRO)
                    .billingInterval(SubscriptionPlanBillingInterval.YEARLY)
                    .price(99.99)
                    .maxShortUrls(100)
                    .maxUsagePerUrl(1000)
                    .description("Pro plan for power users (Yearly)")
                    .active(true)
                    .build(),
                SubscriptionPlan.builder()
                    .type(SubscriptionPlanType.ENTERPRISE)
                    .billingInterval(SubscriptionPlanBillingInterval.YEARLY)
                    .price(999.99)
                    .maxShortUrls(1000)
                    .maxUsagePerUrl(10000)
                    .description("Enterprise plan for businesses (Yearly)")
                    .active(true)
                    .build()
            );

            subscriptionPlanRepository.saveAll(plans);
        }
    }
}
