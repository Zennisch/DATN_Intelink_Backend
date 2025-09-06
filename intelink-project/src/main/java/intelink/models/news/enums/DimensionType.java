package intelink.models.news.enums;

public enum DimensionType {

    // Sources
    REFERRER,
    REFERRER_TYPE,
    UTM_SOURCE,
    UTM_MEDIUM,
    UTM_CAMPAIGN,
    UTM_TERM,
    UTM_CONTENT,

    // Geometrics
    COUNTRY,
    REGION,
    CITY,
    TIMEZONE,

    // Technologies
    BROWSER,
    OS,
    DEVICE_TYPE,
    ISP,
    LANGUAGE,

    // Custom dimensions
    CUSTOM;

    public static DimensionType fromString(String type) {
        try {
            return DimensionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid dimension type: " + type);
        }
    }
}
