package intelink.services.interfaces;

import intelink.dto.request.subscription.RegisterSubscriptionRequest;
import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;

import java.util.List;
import java.util.UUID;

public interface ISubscriptionService {
    List<Subscription> findByUser(User user);

    Subscription findCurrentActiveSubscription(User user);

    Subscription registerSubscription(User user, RegisterSubscriptionRequest request);

    void cancelSubscription(User user, UUID subscriptionId);
}
