package intelink.services.interfaces;

import intelink.dto.request.subscription.RegisterSubscriptionRequest;
import intelink.dto.response.subscription.GetAllSubscriptionsResponse;
import intelink.dto.response.subscription.SubscriptionResponse;
import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;

import java.util.List;
import java.util.UUID;

public interface ISubscriptionService {
    GetAllSubscriptionsResponse findByUser(User user);

    SubscriptionResponse getCurrentActiveSubscriptionForUser(User user);

    Subscription findCurrentActiveSubscription(User user);

    Subscription registerSubscription(User user, RegisterSubscriptionRequest request);

    void cancelSubscription(User user, UUID subscriptionId);
}
