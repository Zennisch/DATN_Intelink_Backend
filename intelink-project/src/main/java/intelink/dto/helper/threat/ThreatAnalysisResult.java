package intelink.dto.helper.threat;

import java.util.List;

public record ThreatAnalysisResult(boolean hasMatches, List<ThreatMatchInfo> matches) {
}
