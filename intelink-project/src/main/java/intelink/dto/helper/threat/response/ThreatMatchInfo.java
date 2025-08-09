package intelink.dto.helper.threat.response;

public record ThreatMatchInfo(
        String threatType,
        String platformType,
        String url,
        String cacheDuration,
        String threatEntryType
) {
    public ThreatMatchInfo {
        if (threatType == null || threatType.isBlank()) {
            throw new IllegalArgumentException("Threat type cannot be null or blank");
        }
        if (platformType == null || platformType.isBlank()) {
            throw new IllegalArgumentException("Platform type cannot be null or blank");
        }
        if (url == null || url.isBlank()) {
            throw new IllegalArgumentException("URL cannot be null or blank");
        }
        if (cacheDuration == null || cacheDuration.isBlank()) {
            throw new IllegalArgumentException("Cache duration cannot be null or blank");
        }
        if (threatEntryType == null || threatEntryType.isBlank()) {
            throw new IllegalArgumentException("Threat entry type cannot be null or blank");
        }
    }
}
