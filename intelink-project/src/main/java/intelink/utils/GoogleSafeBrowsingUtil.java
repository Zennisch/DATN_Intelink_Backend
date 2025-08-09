package intelink.utils;

import intelink.dto.helper.threat.ThreatEntry;
import intelink.dto.helper.threat.enums.PlatformType;
import intelink.dto.helper.threat.enums.ThreatEntryType;
import intelink.dto.helper.threat.enums.ThreatType;
import intelink.dto.helper.threat.request.SafeBrowsingClient;
import intelink.dto.helper.threat.request.SafeBrowsingRequest;
import intelink.dto.helper.threat.request.ThreatInfo;
import intelink.dto.helper.threat.response.ThreatAnalysisResult;
import intelink.dto.helper.threat.response.ThreatMatchInfo;
import intelink.dto.helper.threat.response.ThreatMatchesResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Component
public class GoogleSafeBrowsingUtil {

    @Value("${app.api.key.safe-browsing}")
    private String key;

    public static final String URL_ENDPOINT = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=";
    public static final String CLIENT_ID = "testClient";
    public static final String CLIENT_VERSION = "1.0";

    private final RestTemplate restTemplate;

    public GoogleSafeBrowsingUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ThreatAnalysisResult checkUrls(List<String> urls) {
        SafeBrowsingRequest body = new SafeBrowsingRequest(
                new SafeBrowsingClient(CLIENT_ID, CLIENT_VERSION),
                new ThreatInfo(
                        List.of(ThreatType.MALWARE, ThreatType.SOCIAL_ENGINEERING),
                        List.of(PlatformType.ANY_PLATFORM),
                        List.of(ThreatEntryType.URL),
                        urls.stream().map(ThreatEntry::new).toList()
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<SafeBrowsingRequest> req = new HttpEntity<>(body, headers);

        String urlWithKey = URL_ENDPOINT + key;
        ResponseEntity<ThreatMatchesResponse> resp
                = restTemplate.postForEntity(urlWithKey, req, ThreatMatchesResponse.class);

        ThreatMatchesResponse matchesResp = resp.getBody();
        if (matchesResp == null || matchesResp.matches() == null) {
            return new ThreatAnalysisResult(false, List.of());
        } else {
            List<ThreatMatchInfo> infos = matchesResp.matches().stream()
                    .map(m -> new ThreatMatchInfo(
                            m.threatType(),
                            m.platformType(),
                            m.threat().url(),
                            m.cacheDuration(),
                            m.threatEntryType())
                    )
                    .toList();
            return new ThreatAnalysisResult(true, infos);
        }
    }

}
