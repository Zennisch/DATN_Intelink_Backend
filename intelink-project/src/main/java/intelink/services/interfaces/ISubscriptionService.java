package intelink.services.interfaces;

import intelink.dto.request.subscription.RegisterSubscriptionRequest;
import intelink.dto.response.subscription.GetAllSubscriptionsResponse;
import intelink.dto.response.subscription.SubscriptionCostResponse;
import intelink.dto.response.subscription.SubscriptionResponse;
import intelink.models.Subscription;
import intelink.models.User;

import java.math.BigDecimal;
import java.util.UUID;

public interface ISubscriptionService {
    GetAllSubscriptionsResponse findByUser(User user);

    SubscriptionResponse getCurrentActiveSubscriptionForUser(User user);

    Subscription findCurrentActiveSubscription(User user);

    Subscription registerSubscription(User user, RegisterSubscriptionRequest request) throws Exception;

    void cancelSubscription(User user, UUID subscriptionId);

    BigDecimal calculateAmountToPay(User user, Subscription subscription, RegisterSubscriptionRequest request);

    Subscription createPendingSubscription(User user, RegisterSubscriptionRequest request);

    SubscriptionCostResponse calculateSubscriptionCost(User user, Long subscriptionPlanId, boolean applyImmediately);
}
