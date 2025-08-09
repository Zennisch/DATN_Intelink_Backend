package intelink.dto.helper.threat.request;

import intelink.dto.helper.threat.ThreatEntry;
import intelink.dto.helper.threat.enums.PlatformType;
import intelink.dto.helper.threat.enums.ThreatEntryType;
import intelink.dto.helper.threat.enums.ThreatType;

import java.util.List;

public record ThreatInfo(
        List<ThreatType> threatTypes,
        List<PlatformType> platformTypes,
        List<ThreatEntryType> threatEntryTypes,
        List<ThreatEntry> threatEntries
) {
}