package intelink.controllers;

import intelink.models.enums.DimensionType;
import intelink.services.StatisticsService;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/statistics")
public class StatisticsController {

    private final StatisticsService statisticsService;

    @GetMapping("/{shortCode}/device")
    public ResponseEntity<Map<String, Object>> getDeviceStats(@PathVariable String shortCode) {
        log.info("StatisticsController.getDeviceStats: Getting device stats for short code: {}", shortCode);
        Map<String, Object> stats = statisticsService.getDeviceStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{shortCode}/location")
    public ResponseEntity<Map<String, Object>> getLocationStats(@PathVariable String shortCode) {
        log.info("StatisticsController.getLocationStats: Getting location stats for short code: {}", shortCode);
        Map<String, Object> stats = statisticsService.getLocationStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{shortCode}/time")
    public ResponseEntity<Map<String, Object>> getTimeStats(@PathVariable String shortCode) {
        log.info("StatisticsController.getTimeStats: Getting time stats for short code: {}", shortCode);
        Map<String, Object> stats = statisticsService.getTimeStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{shortCode}/dimension")
    public ResponseEntity<?> getDimensionStats(
            @PathVariable String shortCode,
            @RequestParam String type) {
        log.info("StatisticsController.getDimensionStats: Getting {} dimension stats for short code: {}", type, shortCode);

        try {
            // Convert string type to DimensionType enum with flexible matching
            DimensionType dimensionType;
            try {
                // First try direct enum matching
                dimensionType = DimensionType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                // If direct matching fails, try mapping common aliases
                dimensionType = mapStringToDimensionType(type.toLowerCase());
            }

            Map<String, Object> stats = statisticsService.getDimensionStats(shortCode, dimensionType);
            return ResponseEntity.ok(stats);

        } catch (IllegalArgumentException e) {
            log.error("StatisticsController.getDimensionStats: Invalid dimension type: {}", type);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Invalid dimension type");
            errorResponse.put("message", e.getMessage());
            errorResponse.put("providedType", type);
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * Maps string values to DimensionType enum with flexible matching
     * Supports aliases like "device" -> DEVICE_TYPE, "location" -> COUNTRY, etc.
     */
    private DimensionType mapStringToDimensionType(String type) {
        return switch (type.toLowerCase()) {
            // Device related
            case "device", "device_type", "devicetype" -> DimensionType.DEVICE_TYPE;
            case "browser" -> DimensionType.BROWSER;
            case "os", "operating_system", "operatingsystem" -> DimensionType.OS;

            // Location related
            case "country", "location" -> DimensionType.COUNTRY;
            case "city" -> DimensionType.CITY;
            case "region" -> DimensionType.REGION;
            case "timezone" -> DimensionType.TIMEZONE;

            // Source related
            case "referrer" -> DimensionType.REFERRER;
            case "referrer_type", "referrertype" -> DimensionType.REFERRER_TYPE;
            case "utm_source", "utmsource" -> DimensionType.UTM_SOURCE;
            case "utm_medium", "utmmedium" -> DimensionType.UTM_MEDIUM;
            case "utm_campaign", "utmcampaign" -> DimensionType.UTM_CAMPAIGN;
            case "utm_term", "utmterm" -> DimensionType.UTM_TERM;
            case "utm_content", "utmcontent" -> DimensionType.UTM_CONTENT;

            // Technology related
            case "isp" -> DimensionType.ISP;
            case "language" -> DimensionType.LANGUAGE;

            // Custom
            case "custom" -> DimensionType.CUSTOM;

            default -> throw new IllegalArgumentException(
                    "Unsupported dimension type: " + type +
                            ". Supported types: device, browser, os, country, city, region, timezone, " +
                            "referrer, utm_source, utm_medium, utm_campaign, isp, language, custom"
            );
        };
    }

    @GetMapping("/{shortCode}/overview")
    public ResponseEntity<Map<String, Object>> getOverviewStats(@PathVariable String shortCode) {
        log.info("StatisticsController.getOverviewStats: Getting overview stats for short code: {}", shortCode);
        Map<String, Object> overview = new HashMap<>();
        overview.put("device", statisticsService.getDeviceStats(shortCode));
        overview.put("location", statisticsService.getLocationStats(shortCode));
        overview.put("time", statisticsService.getTimeStats(shortCode));
        return ResponseEntity.ok(overview);
    }
}

