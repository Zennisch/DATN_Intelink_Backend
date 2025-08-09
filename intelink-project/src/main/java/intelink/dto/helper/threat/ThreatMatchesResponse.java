package intelink.dto.helper.threat;

import java.util.List;

public record ThreatMatchesResponse(List<ThreatMatch> matches) {
    public ThreatMatchesResponse {
        if (matches == null || matches.isEmpty()) {
            throw new IllegalArgumentException("Matches cannot be null or empty");
        }
    }
}
