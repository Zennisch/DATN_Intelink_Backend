package intelink.dto.object.threat.enums;

public enum ThreatEntryType {
    THREAT_ENTRY_TYPE_UNSPECIFIED,
    URL,
    EXECUTABLE;

    public static ThreatEntryType fromString(String value) {
        try {
            return ThreatEntryType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unknown ThreatEntryType: " + value, e);
        }
    }
}
