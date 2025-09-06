package intelink.models.news.enums;

public enum ShortUrlAnalysisThreatType {
    MALWARE,
    PHISHING,
    SPAM,
    SCAM,
    OTHER,
    NONE;

    public static ShortUrlAnalysisThreatType fromString(String threatType) {
        try {
            return ShortUrlAnalysisThreatType.valueOf(threatType.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid analysis threat type: " + threatType);
        }
    }
}
