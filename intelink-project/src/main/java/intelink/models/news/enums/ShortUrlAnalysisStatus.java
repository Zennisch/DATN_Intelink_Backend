package intelink.models.news.enums;

public enum ShortUrlAnalysisStatus {
    PENDING, SAFE, MALICIOUS, SUSPICIOUS, MALWARE, SOCIAL_ENGINEERING, UNKNOWN;

    public static ShortUrlAnalysisStatus fromString(String status) {
        try {
            return ShortUrlAnalysisStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid analysis status: " + status);
        }
    }
}
