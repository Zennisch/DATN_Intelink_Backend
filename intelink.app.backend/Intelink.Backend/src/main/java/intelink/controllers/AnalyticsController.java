package intelink.controllers;

import intelink.dto.AnalyticsResponse;
import intelink.models.DailyStat;
import intelink.models.User;
import intelink.models.enums.DimensionType;
import intelink.services.AnalyticsService;
import intelink.services.ShortUrlService;
import intelink.services.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {

    private final AnalyticsService analyticsService;
    private final UserService userService;
    private final ShortUrlService shortUrlService;

    @GetMapping("/{shortCode}/daily")
    public ResponseEntity<List<DailyStat>> getDailyStats(
            @PathVariable String shortCode,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!hasAccessToShortUrl(shortCode, userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        List<DailyStat> stats = analyticsService.getDailyStatsForRange(shortCode, startDate, endDate);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{shortCode}/dimensions")
    public ResponseEntity<AnalyticsResponse> getDimensionStats(
            @PathVariable String shortCode,
            @RequestParam DimensionType type,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!hasAccessToShortUrl(shortCode, userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Map<String, Long> stats = analyticsService.getTopValuesByDimension(shortCode, type);

        AnalyticsResponse response = AnalyticsResponse.builder()
                .shortCode(shortCode)
                .type(type.toString())
                .data(stats)
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}/overview")
    public ResponseEntity<Map<String, Object>> getOverview(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails) {

        if (!hasAccessToShortUrl(shortCode, userDetails)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Get recent stats
        LocalDate today = LocalDate.now();
        LocalDate weekAgo = today.minusDays(7);

        List<DailyStat> weeklyStats = analyticsService.getDailyStatsForRange(shortCode, weekAgo, today);
        Map<String, Long> countryStats = analyticsService.getTopValuesByDimension(shortCode, DimensionType.COUNTRY);
        Map<String, Long> browserStats = analyticsService.getTopValuesByDimension(shortCode, DimensionType.BROWSER);

        long totalClicks = weeklyStats.stream().mapToLong(DailyStat::getClickCount).sum();

        Map<String, Object> overview = Map.of(
                "totalClicks", totalClicks,
                "weeklyStats", weeklyStats,
                "topCountries", countryStats.entrySet().stream().limit(5).toList(),
                "topBrowsers", browserStats.entrySet().stream().limit(5).toList()
        );

        return ResponseEntity.ok(overview);
    }

    private boolean hasAccessToShortUrl(String shortCode, UserDetails userDetails) {
        Optional<User> userOpt = userService.findByUsername(userDetails.getUsername());
        if (userOpt.isEmpty()) {
            return false;
        }

        return shortUrlService.findByShortCode(shortCode)
                .map(shortUrl -> shortUrl.getUser().getId().equals(userOpt.get().getId()))
                .orElse(false);
    }
}