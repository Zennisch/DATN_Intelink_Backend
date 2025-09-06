package intelink.models.news.enums;

public enum ShortUrlAnalysisPlatformType {
    ANY_PLATFORM,
    DESKTOP,
    MOBILE,
    TABLET;

    public static ShortUrlAnalysisPlatformType fromString(String platformType) {
        try {
            return ShortUrlAnalysisPlatformType.valueOf(platformType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid analysis platform type: " + platformType);
        }
    }
}
