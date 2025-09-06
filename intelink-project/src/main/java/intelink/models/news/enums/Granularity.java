package intelink.models.news.enums;

public enum Granularity {
    HOURLY, DAILY, MONTHLY, YEARLY;

    public static Granularity fromString(String value) {
        try {
            return Granularity.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid granularity: " + value);
        }
    }
}
