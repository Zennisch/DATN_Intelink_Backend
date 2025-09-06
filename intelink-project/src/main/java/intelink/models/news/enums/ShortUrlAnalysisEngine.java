package intelink.models.news.enums;

public enum ShortUrlAnalysisEngine {
    GOOGLE_SAFE_BROWSING,
    VIRUSTOTAL,
    PHISHING_DETECTOR;

    public static ShortUrlAnalysisEngine fromString(String engine) {
        try {
            return ShortUrlAnalysisEngine.valueOf(engine.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid analysis engine: " + engine);
        }
    }
}
