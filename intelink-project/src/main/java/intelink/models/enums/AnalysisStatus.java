package intelink.models.enums;

public enum AnalysisStatus {
    PENDING, SAFE, MALICIOUS, SUSPICIOUS, MALWARE, SOCIAL_ENGINEERING, UNKNOWN;

    public static AnalysisStatus fromString(String status) {
        try {
            return AnalysisStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid analysis status: " + status);
        }
    }
}
