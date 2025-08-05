package intelink.utils;

import intelink.dto.helper.threat.ThreatAnalysisResult;
import intelink.dto.helper.threat.ThreatMatchInfo;
import intelink.dto.helper.threat.ThreatMatchesResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Component
public class GoogleSafeBrowsingUtil {

    private final RestTemplate restTemplate;
    @Value("classpath:googleSafeBrowsing/API_KEY.txt")
    private Resource apiKeyResource;
    private String apiKey;

    public GoogleSafeBrowsingUtil(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostConstruct
    private void init() {
        try {
            this.apiKey = StreamUtils.copyToString(
                    apiKeyResource.getInputStream(), StandardCharsets.UTF_8
            ).trim();
        } catch (IOException e) {
            throw new RuntimeException("Failed to read Google Safe Browsing API key", e);
        }
    }

    public ThreatAnalysisResult checkUrls(List<String> urls) {
        String urlEndpoint = "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=" + apiKey;

        Map<String, Object> body = Map.of(
                "client", Map.of("clientId", "testclient", "clientVersion", "1.0"),
                "threatInfo", Map.of(
                        "threatTypes", List.of("MALWARE", "SOCIAL_ENGINEERING"),
                        "platformTypes", List.of("ANY_PLATFORM"),
                        "threatEntryTypes", List.of("URL"),
                        "threatEntries", urls.stream().map(u -> Map.of("url", u)).toList()
                )
        );

        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-Type", "application/json");

        HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, headers);

        ResponseEntity<ThreatMatchesResponse> resp = restTemplate.postForEntity(urlEndpoint, req, ThreatMatchesResponse.class);

        ThreatMatchesResponse matchesResp = resp.getBody();
        if (matchesResp == null || matchesResp.matches == null) {
            return new ThreatAnalysisResult(false, List.of());
        } else {
            List<ThreatMatchInfo> infos = matchesResp.matches.stream()
                    .map(m -> new ThreatMatchInfo(m.threatType, m.platformType, m.threat.url, m.cacheDuration, m.threatEntryType))
                    .toList();
            return new ThreatAnalysisResult(true, infos);
        }
    }

}
