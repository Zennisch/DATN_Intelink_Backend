package intelink.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
@Slf4j
public class GeoLocationUtils {

    @Value("${app.geoip.enabled:false}")
    private boolean geoIpEnabled;

    @Value("${app.geoip.api-key:}")
    private String apiKey;

    @Value("${app.geoip.service-url:}")
    private String serviceUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public Map<String, String> getLocationInfo(String ipAddress) {
        Map<String, String> defaultLocation = Map.of(
            "country", "Unknown",
            "city", "Unknown"
        );

        if (!geoIpEnabled || !IpUtils.isValidIp(ipAddress) || IpUtils.isPrivateIp(ipAddress)) {
            return defaultLocation;
        }

        try {
            // Example integration with IPStack API
            String url = serviceUrl + "/" + ipAddress + "?access_key=" + apiKey;

            // Make API call and parse response
            Map<String, Object> response = restTemplate.getForObject(url, Map.class);

            if (response != null && response.get("country_name") != null) {
                String country = (String) response.get("country_name");
                String city = (String) response.get("city");

                return Map.of(
                    "country", country != null ? country : "Unknown",
                    "city", city != null ? city : "Unknown"
                );
            }

        } catch (Exception e) {
            log.warn("Failed to get location for IP {}: {}", ipAddress, e.getMessage());
        }

        return defaultLocation;
    }
}