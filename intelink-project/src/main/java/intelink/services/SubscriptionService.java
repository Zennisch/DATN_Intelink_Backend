package intelink.services;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import intelink.dto.request.subscription.RegisterSubscriptionRequest;
import intelink.dto.response.subscription.GetAllSubscriptionsResponse;
import intelink.dto.response.subscription.SubscriptionCostResponse;
import intelink.dto.response.subscription.SubscriptionResponse;
import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;
import intelink.models.enums.SubscriptionStatus;
import intelink.repositories.SubscriptionPlanRepository;
import intelink.repositories.SubscriptionRepository;
import intelink.repositories.UserRepository;
import intelink.services.interfaces.ISubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubscriptionService implements ISubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final PaymentService paymentService;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public GetAllSubscriptionsResponse findByUser(User user) {
        List<Subscription> subscriptions = subscriptionRepository.findByUser(user);
        return GetAllSubscriptionsResponse.fromEntities(subscriptions);
    }

    @Override
    @Transactional
    public SubscriptionResponse getCurrentActiveSubscriptionForUser(User user) {
        Subscription subscription = findCurrentActiveSubscription(user);
        return subscription != null
                ? SubscriptionResponse.fromEntities(subscription, subscription.getSubscriptionPlan())
                : null;
    }

    @Override
    @Transactional
    public Subscription findCurrentActiveSubscription(User user) {
        return subscriptionRepository.findActiveSubscriptionByUser(user, Instant.now())
                .orElse(null);
    }

    @Override
    @Transactional
    public Subscription registerSubscription(User user, RegisterSubscriptionRequest request) throws Exception {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getSubscriptionPlanId())
                .orElseThrow(() -> new RuntimeException(
                        "Subscription plan not found with ID: " + request.getSubscriptionPlanId()));

        Subscription currentSubscription = findCurrentActiveSubscription(user);

        BigDecimal proRateValue = BigDecimal.ZERO;
        BigDecimal planPrice = plan.getPrice();
        BigDecimal amountToPay = planPrice;

        // Nếu có subscription hiện tại và chọn áp dụng ngay
        log.info("User {} is registering for plan {} with applyImmediately={}",
                user.getId(), plan.getType(), request.getApplyImmediately());
        if (currentSubscription != null && request.getApplyImmediately()) {
            SubscriptionPlan currentPlan = currentSubscription.getSubscriptionPlan();
            if (!currentPlan.getType().name().equals("FREE")) {
                Instant now = Instant.now();
                Instant expiresAt = currentSubscription.getExpiresAt();
                long totalDays = ChronoUnit.DAYS.between(currentSubscription.getStartsAt(), expiresAt);
                long daysLeft = ChronoUnit.DAYS.between(now, expiresAt);

                if (daysLeft > 0 && totalDays > 0) {
                    BigDecimal dailyPrice = currentPlan.getPrice().divide(BigDecimal.valueOf(totalDays), 2,
                            BigDecimal.ROUND_HALF_UP);
                    proRateValue = dailyPrice.multiply(BigDecimal.valueOf(daysLeft));
                    log.info("Pro-rate value for user {} switching from plan {} to {}: {} ({} days left)",
                            user.getId(), currentPlan.getType(), plan.getType(), proRateValue, daysLeft);
                }

                // Tính số tiền cần thanh toán
                amountToPay = planPrice.subtract(proRateValue);
                if (amountToPay.compareTo(BigDecimal.ZERO) < 0) {
                    // Nếu dư, cộng vào creditBalance
                    user.setCreditBalance(user.getCreditBalance() + amountToPay.abs().doubleValue());
                    userRepository.save(user);
                    amountToPay = BigDecimal.ZERO;
                    log.info("User {} has a credit balance of {} after switching plans",
                            user.getId(), user.getCreditBalance());
                }
            }

            // Deactivate subscription hiện tại
            currentSubscription.setActive(false);
            currentSubscription.setStatus(SubscriptionStatus.CANCELED);
            Subscription oldSubs = subscriptionRepository.save(currentSubscription);
            log.info("Deactivated current subscription {} for user {}",
                    oldSubs.getId(), user.getId());
        }

        // Tính ngày hết hạn
        Instant expiresAt = null;
        if (!plan.getType().name().equals("FREE")) {
            switch (plan.getBillingInterval()) {
                case MONTHLY -> expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
                case YEARLY -> expiresAt = Instant.now().plus(365, ChronoUnit.DAYS);
                default -> throw new RuntimeException("Unsupported billing interval: " + plan.getBillingInterval());
            }
        }

        // Tạo subscription mới
        boolean applyImmediately = Boolean.TRUE.equals(request.getApplyImmediately());
        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionPlan(plan)
                .status(applyImmediately ? SubscriptionStatus.ACTIVE : SubscriptionStatus.TRIALING)
                .active(applyImmediately)
                .startsAt(Instant.now())
                .expiresAt(expiresAt)
                .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Tạo payment cho subscription mới
        if (!plan.getType().name().equals("FREE") && applyImmediately && amountToPay.compareTo(BigDecimal.ZERO) > 0) {
            log.info("Creating payment for subscription {} for user {}. Amount to pay: {}",
                    savedSubscription.getId(), user.getId(), amountToPay);
            paymentService.createVnpayPayment(savedSubscription, amountToPay, user.getCurrency(), new HashMap<>());
        }

        log.info("Created new subscription {} for user {} with plan {}. Pro-rate value: {}, Amount to pay: {}",
                savedSubscription.getId(), user.getId(), plan.getType(), proRateValue, amountToPay);

        return savedSubscription;
    }

    @Transactional
    public Subscription createPendingSubscription(User user, RegisterSubscriptionRequest request) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(request.getSubscriptionPlanId())
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        // Tính ngày hết hạn
        Instant expiresAt = null;
        if (!plan.getType().name().equals("FREE")) {
            switch (plan.getBillingInterval()) {
                case MONTHLY -> expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
                case YEARLY -> expiresAt = Instant.now().plus(365, ChronoUnit.DAYS);
                default -> throw new RuntimeException("Unsupported billing interval");
            }
        }

        Subscription subscription = Subscription.builder()
                .user(user)
                .subscriptionPlan(plan)
                .status(SubscriptionStatus.PENDING)
                .active(false)
                .startsAt(Instant.now())
                .expiresAt(expiresAt)
                .build();

        return subscriptionRepository.save(subscription);
    }

    @Transactional
    public BigDecimal calculateAmountToPay(User user, Subscription subscription, RegisterSubscriptionRequest request) {
        // Tính toán pro-rate nếu có subscription hiện tại và applyImmediately
        Subscription currentSubscription = findCurrentActiveSubscription(user);
        BigDecimal proRateValue = BigDecimal.ZERO;
        BigDecimal planPrice = subscription.getSubscriptionPlan().getPrice();
        BigDecimal amountToPay = planPrice;

        if (currentSubscription != null && Boolean.TRUE.equals(request.getApplyImmediately())) {
            SubscriptionPlan currentPlan = currentSubscription.getSubscriptionPlan();
            if (!currentPlan.getType().name().equals("FREE")) {
                Instant now = Instant.now();
                Instant expiresAt = currentSubscription.getExpiresAt();
                long totalDays = ChronoUnit.DAYS.between(currentSubscription.getStartsAt(), expiresAt);
                long daysLeft = ChronoUnit.DAYS.between(now, expiresAt);

                if (daysLeft > 0 && totalDays > 0) {
                    BigDecimal dailyPrice = currentPlan.getPrice().divide(BigDecimal.valueOf(totalDays), 2,
                            BigDecimal.ROUND_HALF_UP);
                    proRateValue = dailyPrice.multiply(BigDecimal.valueOf(daysLeft));
                }

                amountToPay = planPrice.subtract(proRateValue);
                if (amountToPay.compareTo(BigDecimal.ZERO) < 0) {
                    user.setCreditBalance(user.getCreditBalance() + amountToPay.abs().doubleValue());
                    userRepository.save(user);
                    amountToPay = BigDecimal.ZERO;
                }
            }
        }
        return amountToPay;
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

    @Transactional
    public SubscriptionCostResponse calculateSubscriptionCost(User user, Long subscriptionPlanId,
            boolean applyImmediately) {
        SubscriptionPlan plan = subscriptionPlanRepository.findById(subscriptionPlanId)
                .orElseThrow(() -> new RuntimeException("Subscription plan not found"));

        Subscription currentSubscription = findCurrentActiveSubscription(user);

        BigDecimal proRateValue = BigDecimal.ZERO;
        BigDecimal planPrice = plan.getPrice();
        BigDecimal amountToPay = planPrice;
        double creditBalance = user.getCreditBalance();

        String message = "OK";
        Instant startDate;

        if (currentSubscription != null) {
            if (applyImmediately) {
                Instant now = Instant.now();
                SubscriptionPlan currentPlan = currentSubscription.getSubscriptionPlan();
                if (!currentPlan.getType().name().equals("FREE")) {
                    Instant expiresAt = currentSubscription.getExpiresAt();
                    long totalDays = ChronoUnit.DAYS.between(currentSubscription.getStartsAt(), expiresAt);
                    long daysLeft = ChronoUnit.DAYS.between(now, expiresAt);

                    if (daysLeft > 0 && totalDays > 0) {
                        BigDecimal dailyPrice = currentPlan.getPrice().divide(BigDecimal.valueOf(totalDays), 2,
                                BigDecimal.ROUND_HALF_UP);
                        proRateValue = dailyPrice.multiply(BigDecimal.valueOf(daysLeft));
                    }

                    amountToPay = planPrice.subtract(proRateValue);
                    if (amountToPay.compareTo(BigDecimal.ZERO) < 0) {
                        creditBalance += amountToPay.abs().doubleValue();
                        amountToPay = BigDecimal.ZERO;
                        message = "Bạn sẽ được cộng vào credit sau khi thanh toán.";
                    }
                }
                startDate = now;
            } else {
                // Nếu không áp dụng ngay, ngày bắt đầu là ngày hết hạn của subscription hiện
                // tại
                startDate = currentSubscription.getExpiresAt();
            }
        } else {
            // Nếu chưa có subscription, ngày bắt đầu là hiện tại
            startDate = Instant.now();
        }

        return SubscriptionCostResponse.builder()
                .subscriptionPlanId(subscriptionPlanId)
                .planPrice(planPrice)
                .proRateValue(proRateValue)
                .amountToPay(amountToPay)
                .creditBalance(creditBalance)
                .currency(user.getCurrency())
                .message(message)
                .startDate(startDate)
                .build();
    }
}
