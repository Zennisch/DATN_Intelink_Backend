package intelink.dto.helper.threat.response;

import intelink.dto.helper.threat.ThreatEntry;

public record ThreatMatch(
        String threatType,
        String platformType,
        ThreatEntry threat,
        String cacheDuration,
        String threatEntryType
) {
    public ThreatMatch {
        if (threat == null) {
            throw new IllegalArgumentException("Threat entry cannot be null");
        }
        if (threatType == null || threatType.isBlank()) {
            throw new IllegalArgumentException("Threat type cannot be null or blank");
        }
        if (platformType == null || platformType.isBlank()) {
            throw new IllegalArgumentException("Platform type cannot be null or blank");
        }
        if (cacheDuration == null || cacheDuration.isBlank()) {
            throw new IllegalArgumentException("Cache duration cannot be null or blank");
        }
        if (threatEntryType == null || threatEntryType.isBlank()) {
            throw new IllegalArgumentException("Threat entry type cannot be null or blank");
        }
    }
}
