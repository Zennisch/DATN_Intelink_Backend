package intelink.models.enums;

public enum SubscriptionPlanType {
    FREE, PRO, ENTERPRISE;

    public static SubscriptionPlanType fromString(String type) {
        try {
            return SubscriptionPlanType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid subscription plan type: " + type);
        }
    }
}
