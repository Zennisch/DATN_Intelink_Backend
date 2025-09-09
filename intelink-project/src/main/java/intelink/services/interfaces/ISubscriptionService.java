package intelink.services.interfaces;

import intelink.dto.request.subscription.CreateSubscriptionRequest;
import intelink.models.Subscription;
import intelink.models.User;

import java.util.List;
import java.util.UUID;

public interface ISubscriptionService {
    List<Subscription> findByUser(User user);
    Subscription findCurrentActiveSubscription(User user);
    Subscription createSubscription(User user, CreateSubscriptionRequest request);
    void cancelSubscription(User user, UUID subscriptionId);
}
