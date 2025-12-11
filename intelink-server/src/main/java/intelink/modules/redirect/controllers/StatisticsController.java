package intelink.modules.redirect.controllers;

import intelink.dto.statistics.DimensionStatResponse;
import intelink.dto.statistics.TimeSeriesStatResponse;
import intelink.models.User;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.modules.auth.services.AuthService;
import intelink.modules.redirect.services.StatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;
    private final AuthService authService;

    @GetMapping("/{shortCode}/browser")
    public ResponseEntity<?> getBrowserStats(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        DimensionStatResponse response = statisticsService.getDimensionStats(user, shortCode, DimensionType.BROWSER);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}/os")
    public ResponseEntity<?> getOsStats(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        DimensionStatResponse response = statisticsService.getDimensionStats(user, shortCode, DimensionType.OS);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}/device")
    public ResponseEntity<?> getDeviceStats(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        DimensionStatResponse response = statisticsService.getDimensionStats(user, shortCode, DimensionType.DEVICE_TYPE);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}/country")
    public ResponseEntity<?> getCountryStats(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        DimensionStatResponse response = statisticsService.getDimensionStats(user, shortCode, DimensionType.COUNTRY);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}/city")
    public ResponseEntity<?> getCityStats(
            @PathVariable String shortCode,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        DimensionStatResponse response = statisticsService.getDimensionStats(user, shortCode, DimensionType.CITY);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{shortCode}/timeseries")
    public ResponseEntity<?> getTimeSeriesStats(
            @PathVariable String shortCode,
            @RequestParam(required = false) Granularity granularity,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false) String timezone,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User user = authService.getCurrentUser(userDetails);
        TimeSeriesStatResponse response = statisticsService.getTimeSeriesStats(
                user, shortCode, granularity, from, to, timezone);
        return ResponseEntity.ok(response);
    }

}
