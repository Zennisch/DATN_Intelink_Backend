package intelink.dto.helper.threat;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ThreatAnalysisResult {
    public final boolean hasMatches;
    public final List<ThreatMatchInfo> matches;
}
