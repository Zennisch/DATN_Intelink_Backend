package intelink.models.enums;

public enum PaymentMethod {
    CREDIT_CARD, PAYPAL, BANK_TRANSFER, VNPAY, MOMO, ZALOPAY;

    public static PaymentMethod fromString(String method) {
        try {
            return PaymentMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment method: " + method);
        }
    }
}
