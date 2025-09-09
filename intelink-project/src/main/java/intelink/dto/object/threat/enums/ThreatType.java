package intelink.dto.object.threat.enums;

public enum ThreatType {
    THREAT_TYPE_UNSPECIFIED,
    MALWARE,
    SOCIAL_ENGINEERING,
    UNWANTED_SOFTWARE,
    POTENTIALLY_HARMFUL_APPLICATION;

    public static ThreatType fromString(String value) {
        try {
            return ThreatType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown ThreatType: " + value, e);
        }
    }
}
