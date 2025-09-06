package intelink.models.news.enums;

public enum PaymentProvider {
    CREDIT_CARD, PAYPAL, BANK_TRANSFER, VNPAY, MOMO, ZALOPAY;

    public static PaymentProvider fromString(String provider) {
        try {
            return PaymentProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment provider: " + provider);
        }
    }
}
