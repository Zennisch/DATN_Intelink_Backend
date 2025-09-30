package intelink.utils.seeder;

import intelink.models.SubscriptionPlan;
import intelink.models.enums.SubscriptionPlanBillingInterval;
import intelink.models.enums.SubscriptionPlanType;
import intelink.repositories.SubscriptionPlanRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class SubscriptionPlanDataSeeder {

    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public List<SubscriptionPlan> createDefaultPlans() {
        log.info("Creating default subscription plans...");

        // Free Plan
        SubscriptionPlan freePlan = SubscriptionPlan.builder()
                .type(SubscriptionPlanType.FREE)
                .description("Free plan with basic features for personal use")
                .price(BigDecimal.ZERO)
                .billingInterval(SubscriptionPlanBillingInterval.MONTHLY)
                .maxShortUrls(10)
                .shortCodeCustomizationEnabled(false)
                .statisticsEnabled(false)
                .customDomainEnabled(false)
                .apiAccessEnabled(false)
                .active(true)
                .max_usage_per_url(100)
                .build();

        // Pro Plan
        SubscriptionPlan proPlan = SubscriptionPlan.builder()
                .type(SubscriptionPlanType.PRO)
                .description("Professional plan with advanced features for businesses")
                .price(new BigDecimal("109900"))
                .billingInterval(SubscriptionPlanBillingInterval.MONTHLY)
                .maxShortUrls(100)
                .shortCodeCustomizationEnabled(true)
                .statisticsEnabled(true)
                .customDomainEnabled(false)
                .apiAccessEnabled(false)
                .active(true)
                .max_usage_per_url(1000)
                .build();

        // Enterprise Plan
        SubscriptionPlan enterprisePlan = SubscriptionPlan.builder()
                .type(SubscriptionPlanType.ENTERPRISE)
                .description("Enterprise plan with unlimited features for large organizations")
                .price(new BigDecimal("1099000"))
                .billingInterval(SubscriptionPlanBillingInterval.MONTHLY)
                .maxShortUrls(-1) // -1 means unlimited
                .shortCodeCustomizationEnabled(true)
                .statisticsEnabled(true)
                .customDomainEnabled(true)
                .apiAccessEnabled(true)
                .active(true)
                .max_usage_per_url(-1) // -1 means unlimited
                .build();

        List<SubscriptionPlan> plans = List.of(freePlan, proPlan, enterprisePlan);
        List<SubscriptionPlan> savedPlans = subscriptionPlanRepository.saveAll(plans);

        log.info("Created {} subscription plans", savedPlans.size());
        return savedPlans;
    }
}
