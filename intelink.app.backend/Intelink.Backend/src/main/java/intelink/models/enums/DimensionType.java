package intelink.models.enums;

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
}
