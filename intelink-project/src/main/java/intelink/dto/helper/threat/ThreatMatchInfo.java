package intelink.dto.helper.threat;

public record ThreatMatchInfo(String threatType, String platformType, String url, String cacheDuration,
                              String threatEntryType) {
}
