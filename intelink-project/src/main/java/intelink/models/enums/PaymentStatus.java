package intelink.models.enums;

public enum PaymentStatus {
    PENDING, COMPLETED, FAILED, CANCELLED, REFUNDED;

    public static PaymentStatus fromString(String status) {
        try {
            return PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment status: " + status);
        }
    }
}
