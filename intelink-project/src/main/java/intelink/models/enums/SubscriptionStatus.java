package intelink.models.enums;

public enum SubscriptionStatus {
    ACTIVE, TRIALING, PAST_DUE, CANCELED, EXPIRED;

    public static SubscriptionStatus fromString(String status) {
        try {
            return SubscriptionStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid subscription status: " + status);
        }
    }
}
