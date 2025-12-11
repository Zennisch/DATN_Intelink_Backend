package intelink.modules.utils.services;

import intelink.dto.safebrowsing.SafeBrowsingRequest;
import intelink.dto.safebrowsing.SafeBrowsingResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
@Slf4j
public class SafeBrowsingService {

    @Value("${app.safebrowsing.api.key:}")
    private String apiKey;

    @Value("${app.safebrowsing.api.url:https://safebrowsing.googleapis.com/v4/threatMatches:find}")
    private String apiUrl;

    @Value("${app.safebrowsing.client.id:intelink}")
    private String clientId;

    @Value("${app.safebrowsing.client.version:1.0.0}")
    private String clientVersion;

    private final RestTemplate restTemplate;

    public SafeBrowsingService() {
        this.restTemplate = new RestTemplate();
    }

    public SafeBrowsingResponse checkUrl(String url) {
        if (apiKey == null || apiKey.isBlank()) {
            log.warn("[SafeBrowsingService.checkUrl] API key is not configured. Skipping URL check.");
            return SafeBrowsingResponse.builder().matches(List.of()).build();
        }

        try {
            SafeBrowsingRequest request = buildRequest(url);
            
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<SafeBrowsingRequest> entity = new HttpEntity<>(request, headers);
            
            String urlWithKey = apiUrl + "?key=" + apiKey;
            
            ResponseEntity<SafeBrowsingResponse> response = restTemplate.postForEntity(
                urlWithKey,
                entity,
                SafeBrowsingResponse.class
            );

            SafeBrowsingResponse responseBody = response.getBody();
            
            if (responseBody == null || responseBody.getMatches() == null) {
                return SafeBrowsingResponse.builder().matches(List.of()).build();
            }

            return responseBody;
            
        } catch (Exception e) {
            log.error("[SafeBrowsingService.checkUrl] Error checking URL {}: {}", url, e.getMessage(), e);
            return SafeBrowsingResponse.builder().matches(List.of()).build();
        }
    }

    private SafeBrowsingRequest buildRequest(String url) {
        SafeBrowsingRequest.Client client = SafeBrowsingRequest.Client.builder()
                .clientId(clientId)
                .clientVersion(clientVersion)
                .build();

        SafeBrowsingRequest.ThreatEntry threatEntry = SafeBrowsingRequest.ThreatEntry.builder()
                .url(url)
                .build();

        SafeBrowsingRequest.ThreatInfo threatInfo = SafeBrowsingRequest.ThreatInfo.builder()
                .threatTypes(List.of("MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"))
                .platformTypes(List.of("ANY_PLATFORM"))
                .threatEntryTypes(List.of("URL"))
                .threatEntries(List.of(threatEntry))
                .build();

        return SafeBrowsingRequest.builder()
                .client(client)
                .threatInfo(threatInfo)
                .build();
    }
}
