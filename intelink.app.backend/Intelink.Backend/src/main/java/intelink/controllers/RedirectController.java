package intelink.controllers;

import intelink.models.ShortUrl;
import intelink.services.ClickLogService;
import intelink.services.ShortUrlService;
import intelink.services.AnalyticsService;
import intelink.utils.UserAgentUtils;
import intelink.utils.IpUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
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
            String ipAddress = IpUtils.getClientIpAddress(request);
            String userAgent = request.getHeader("User-Agent");
            String referrer = request.getHeader("Referer");

            // Parse user agent
            Map<String, String> agentInfo = UserAgentUtils.parseUserAgent(userAgent);

            // Record click
            clickLogService.recordClick(
                    shortCode,
                    ipAddress,
                    userAgent,
                    referrer,
                    agentInfo.get("country"),
                    agentInfo.get("city"),
                    agentInfo.get("browser"),
                    agentInfo.get("os"),
                    agentInfo.get("deviceType")
            );

            // Update counters
            shortUrlService.incrementClickCount(shortCode);

            // Update analytics
            LocalDate today = LocalDate.now();
            int currentHour = java.time.LocalTime.now().getHour();
            analyticsService.updateDailyStat(shortCode, today, currentHour);

            // Update dimension stats
            if (agentInfo.get("country") != null) {
                analyticsService.updateDimensionStat(shortCode, today,
                        intelink.models.enums.DimensionType.COUNTRY, agentInfo.get("country"));
            }
            if (agentInfo.get("browser") != null) {
                analyticsService.updateDimensionStat(shortCode, today,
                        intelink.models.enums.DimensionType.BROWSER, agentInfo.get("browser"));
            }

        } catch (Exception e) {
            log.error("Failed to record click for shortCode: {}", shortCode, e);
        }
    }
}