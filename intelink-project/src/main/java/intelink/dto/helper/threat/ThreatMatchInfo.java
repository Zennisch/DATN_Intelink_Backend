package intelink.dto.helper.threat;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ThreatMatchInfo {
    public final String threatType;
    public final String platformType;
    public final String url;
    public final String cacheDuration;
    public final String threatEntryType;
}
