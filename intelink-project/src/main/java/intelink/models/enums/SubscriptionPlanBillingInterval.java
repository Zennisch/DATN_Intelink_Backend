package intelink.models.enums;

public enum SubscriptionPlanBillingInterval {
    MONTHLY, YEARLY, NONE;

    public static SubscriptionPlanBillingInterval fromString(String type) {
        try {
            return SubscriptionPlanBillingInterval.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid subscription plan billing interval: " + type);
        }
    }
}
