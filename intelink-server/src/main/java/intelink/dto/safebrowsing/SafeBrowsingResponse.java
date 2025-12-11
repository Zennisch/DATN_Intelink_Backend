package intelink.dto.safebrowsing;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafeBrowsingResponse {

    private List<ThreatMatch> matches;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ThreatMatch {
        private String threatType;
        private String platformType;
        private String threatEntryType;
        private Threat threat;
        private String cacheDuration;
        private ThreatEntryMetadata threatEntryMetadata;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Threat {
        private String url;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ThreatEntryMetadata {
        private List<MetadataEntry> entries;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MetadataEntry {
        private String key;
        private String value;
    }
}
