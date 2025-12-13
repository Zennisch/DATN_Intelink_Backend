package intelink.dto.safebrowsing;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SafeBrowsingRequest {

    private Client client;
    private ThreatInfo threatInfo;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Client {
        private String clientId;
        private String clientVersion;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ThreatInfo {
        private List<String> threatTypes;
        private List<String> platformTypes;
        private List<String> threatEntryTypes;
        private List<ThreatEntry> threatEntries;
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ThreatEntry {
        private String url;
    }
}
