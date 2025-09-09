package intelink.dto.request.analysis;

import intelink.dto.object.threat.ThreatEntry;
import intelink.dto.object.threat.enums.PlatformType;
import intelink.dto.object.threat.enums.ThreatEntryType;
import intelink.dto.object.threat.enums.ThreatType;

import java.util.List;

public record ThreatInfo(
        List<ThreatType> threatTypes,
        List<PlatformType> platformTypes,
        List<ThreatEntryType> threatEntryTypes,
        List<ThreatEntry> threatEntries
) {
}