package intelink.utils.dataseeding;

import intelink.models.Payment;
import intelink.models.PremiumPlan;
import intelink.models.PremiumSubscription;
import intelink.models.User;
import intelink.models.enums.PaymentMethod;
import intelink.models.enums.PaymentStatus;
import intelink.repositories.PaymentRepository;
import intelink.repositories.PremiumPlanRepository;
import intelink.repositories.PremiumSubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class PremiumDataSeeder {

    private final PremiumPlanRepository premiumPlanRepository;
    private final PaymentRepository paymentRepository;
    private final PremiumSubscriptionRepository premiumSubscriptionRepository;
    private final DataSeedingUtils utils;

    public List<PremiumPlan> createPremiumPlans() {
        log.info("Creating premium plans...");
        List<PremiumPlan> plans = new ArrayList<>();

        PremiumPlan basicPlan = PremiumPlan.builder()
                .name("Basic")
                .description("Basic premium plan with enhanced features")
                .price(new BigDecimal("99000"))
                .durationDays(30)
                .maxShortUrls(1000)
                .customDomainEnabled(true)
                .analyticsEnabled(true)
                .apiAccessEnabled(false)
                .active(true)
                .build();

        PremiumPlan proPlan = PremiumPlan.builder()
                .name("Pro")
                .description("Professional plan with advanced analytics")
                .price(new BigDecimal("199000"))
                .durationDays(30)
                .maxShortUrls(5000)
                .customDomainEnabled(true)
                .analyticsEnabled(true)
                .apiAccessEnabled(true)
                .active(true)
                .build();

        PremiumPlan enterprisePlan = PremiumPlan.builder()
                .name("Enterprise")
                .description("Enterprise plan with unlimited features")
                .price(new BigDecimal("499000"))
                .durationDays(30)
                .maxShortUrls(-1)
                .customDomainEnabled(true)
                .analyticsEnabled(true)
                .apiAccessEnabled(true)
                .active(true)
                .build();

        plans.add(basicPlan);
        plans.add(proPlan);
        plans.add(enterprisePlan);

        return premiumPlanRepository.saveAll(plans);
    }

    public List<Payment> createPayments(List<User> users, int count) {
        log.info("Creating {} payments...", count);
        List<Payment> payments = new ArrayList<>();

        PaymentMethod[] methods = PaymentMethod.values();
        PaymentStatus[] statuses = PaymentStatus.values();

        for (int i = 0; i < count; i++) {
            User randomUser = utils.getRandomElement(users);
            Instant createdAt = utils.getRandomInstantBetween(2023, 2024);

            Payment payment = Payment.builder()
                    .transactionId("TXN_" + UUID.randomUUID().toString().substring(0, 8).toUpperCase())
                    .amount(new BigDecimal(utils.getRandom().nextInt(400) + 99).multiply(new BigDecimal("1000")))
                    .currency("VND")
                    .paymentMethod(utils.getRandomElement(List.of(methods)))
                    .status(utils.getRandomElement(List.of(statuses)))
                    .paymentGatewayReference("GW_" + UUID.randomUUID().toString().substring(0, 12))
                    .description("Premium subscription payment")
                    .metadata("{\"user_id\": " + randomUser.getId() + ", \"plan\": \"premium\"}")
                    .processedAt(utils.getRandom().nextDouble() < 0.8 ? 
                        createdAt.plus(utils.getRandom().nextInt(60), ChronoUnit.MINUTES) : null)
                    .createdAt(createdAt)
                    .updatedAt(utils.getRandomInstantAfter(createdAt))
                    .build();

            payments.add(payment);
        }

        return paymentRepository.saveAll(payments);
    }

    public void createPremiumSubscriptions(List<User> users, List<PremiumPlan> plans, List<Payment> payments, int count) {
        log.info("Creating {} premium subscriptions...", count);
        List<PremiumSubscription> subscriptions = new ArrayList<>();

        List<Payment> completedPayments = payments.stream()
                .filter(p -> p.getStatus() == PaymentStatus.COMPLETED)
                .toList();

        for (int i = 0; i < Math.min(count, completedPayments.size()); i++) {
            User randomUser = utils.getRandomElement(users);
            PremiumPlan randomPlan = utils.getRandomElement(plans);
            Payment payment = completedPayments.get(i);
            
            Instant startsAt = payment.getCreatedAt();
            Instant expiresAt = startsAt.plus(randomPlan.getDurationDays(), ChronoUnit.DAYS);

            PremiumSubscription subscription = PremiumSubscription.builder()
                    .startsAt(startsAt)
                    .expiresAt(expiresAt)
                    .active(Instant.now().isBefore(expiresAt))
                    .user(randomUser)
                    .premiumPlan(randomPlan)
                    .payment(payment)
                    .build();

            subscriptions.add(subscription);
        }

        premiumSubscriptionRepository.saveAll(subscriptions);
    }
}
