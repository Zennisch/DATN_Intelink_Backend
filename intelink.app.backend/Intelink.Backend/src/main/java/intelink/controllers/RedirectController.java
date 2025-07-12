package intelink.controllers;

import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import intelink.models.enums.IpVersion;
import intelink.services.AnalyticsService;
import intelink.services.ClickLogService;
import intelink.services.ShortUrlService;
import intelink.utils.IpUtils;
import intelink.utils.UserAgentUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Map;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@Slf4j
public class RedirectController {

    private final ShortUrlService shortUrlService;
    private final ClickLogService clickLogService;
    private final AnalyticsService analyticsService;

    @GetMapping("/{shortCode}")
    public ResponseEntity<?> redirect(@PathVariable String shortCode,
                                      @RequestParam(required = false) String password,
                                      HttpServletRequest request) {

        Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCode(shortCode);

        if (shortUrlOpt.isEmpty()) {
            log.warn("Short URL not found: {}", shortCode);
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Short URL not found"));
        }

        ShortUrl shortUrl = shortUrlOpt.get();

        // Kiểm tra accessibility
        if (!shortUrlService.isUrlAccessible(shortUrl, password)) {
            if (shortUrl.getPassword() != null && !shortUrl.getPassword().equals(password)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Password required", "requiresPassword", true));
            }
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(Map.of("error", "URL is no longer accessible"));
        }

        // Record click và analytics
        recordClickAsync(shortCode, request);

        // Redirect
        return ResponseEntity.status(HttpStatus.FOUND)
                .header("Location", shortUrl.getOriginalUrl())
                .build();
    }

    @PostMapping("/{shortCode}/unlock")
    public ResponseEntity<?> unlockPasswordProtected(@PathVariable String shortCode,
                                                     @RequestBody Map<String, String> payload,
                                                     HttpServletRequest request) {

        String password = payload.get("password");
        Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCode(shortCode);

        if (shortUrlOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        ShortUrl shortUrl = shortUrlOpt.get();

        if (shortUrlService.isUrlAccessible(shortUrl, password)) {
            recordClickAsync(shortCode, request);
            return ResponseEntity.ok(Map.of("redirectUrl", shortUrl.getOriginalUrl()));
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Invalid password"));
    }

    private void recordClickAsync(String shortCode, HttpServletRequest request) {
        try {
            IpUtils.IpProcessingResult ipProcessResult = IpUtils.processClientIp(request);
            String ipAddress = ipProcessResult.getOriginalIp();
            IpVersion ipVersion = ipProcessResult.getIpVersion();
            String normalizedIp = ipProcessResult.getNormalizedIp();
            String subnet = ipProcessResult.getSubnet();
            String userAgent = request.getHeader("User-Agent");
            String referrer = request.getHeader("Referer");

            Map<String, String> agentInfo = UserAgentUtils.parseUserAgent(userAgent);

            clickLogService.recordClick(
                    shortCode,
                    ipAddress,
                    ipVersion,
                    normalizedIp,
                    subnet,
                    userAgent,
                    referrer,
                    agentInfo.get("country"),
                    agentInfo.get("city"),
                    agentInfo.get("browser"),
                    agentInfo.get("os"),
                    agentInfo.get("deviceType")
            );

            shortUrlService.incrementClickCount(shortCode);

            LocalDate today = LocalDate.now();
            int currentHour = LocalTime.now().getHour();

            Map<DimensionType, String> dimensions = Map.of(
                    DimensionType.COUNTRY, Optional.ofNullable(agentInfo.get("country")).orElse("unknown"),
                    DimensionType.BROWSER, Optional.ofNullable(agentInfo.get("browser")).orElse("unknown"),
                    DimensionType.DEVICE_TYPE, Optional.ofNullable(agentInfo.get("deviceType")).orElse("unknown")
            );

            analyticsService.updateStatsForClick(
                    shortCode,
                    today,
                    currentHour,
                    dimensions
            );

        } catch (Exception e) {
            log.error("Failed to record click for shortCode: {}", shortCode, e);
        }
    }
}