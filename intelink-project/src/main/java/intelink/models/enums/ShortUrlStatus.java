package intelink.models.enums;

public enum ShortUrlStatus {
    ENABLED, DISABLED, DELETED;

    public static ShortUrlStatus fromString(String status) {
        try {
            return ShortUrlStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid short URL status: " + status);
        }
    }
}
