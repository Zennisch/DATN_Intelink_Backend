package intelink.dto.helper.threat;

import java.util.List;

public record ThreatAnalysisResult(
        boolean hasMatches,
        List<ThreatMatchInfo> matches
) {
    public ThreatAnalysisResult {
        if (matches == null || matches.isEmpty()) {
            throw new IllegalArgumentException("Matches cannot be null or empty");
        }
    }
}
