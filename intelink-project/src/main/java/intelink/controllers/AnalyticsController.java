package intelink.controllers;

import intelink.dto.response.AnalyticsResponse;
import intelink.models.enums.DimensionType;
import intelink.services.AnalyticService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/analytics")
public class AnalyticsController {

    private final AnalyticService analyticService;

    @GetMapping("/{shortCode}/device")
    public ResponseEntity<ArrayList<Map<String, Object>>> getDeviceStats(@PathVariable String shortCode) {
        log.info("AnalyticsController.getDeviceStats: Getting device stats for short code: {}", shortCode);
        ArrayList<Map<String, Object>> stats = analyticService.getDeviceStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{shortCode}/location")
    public ResponseEntity<ArrayList<Map<String, Object>>> getLocationStats(@PathVariable String shortCode) {
        log.info("AnalyticsController.getLocationStats: Getting location stats for short code: {}", shortCode);
        ArrayList<Map<String, Object>> stats = analyticService.getLocationStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{shortCode}/time")
    public ResponseEntity<ArrayList<Map<String, Object>>> getTimeStats(@PathVariable String shortCode) {
        log.info("AnalyticsController.getTimeStats: Getting time stats for short code: {}", shortCode);
        ArrayList<Map<String, Object>> stats = analyticService.getTimeStats(shortCode);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/{shortCode}/dimension")
    public ResponseEntity<?> getDimensionStats(
            @PathVariable String shortCode,
            @RequestParam String type) {
        log.info("AnalyticsController.getDimensionStats: Getting {} dimension stats for short code: {}", type, shortCode);
        
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
            
            ArrayList<Map<String, Object>> stats = analyticService.getDimensionStats(shortCode, dimensionType);
            return ResponseEntity.ok(stats);
            
        } catch (IllegalArgumentException e) {
            log.error("AnalyticsController.getDimensionStats: Invalid dimension type: {}", type);
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

//    // Additional endpoints for specific dimension types without query parameters
//    @GetMapping("/{shortCode}/dimension/device")
//    public ResponseEntity<ArrayList<Map<String, Object>>> getDeviceTypeDimensionStats(@PathVariable String shortCode) {
//        log.info("AnalyticsController.getDeviceTypeDimensionStats: Getting device dimension stats for short code: {}", shortCode);
//        ArrayList<Map<String, Object>> stats = analyticService.getDimensionStats(shortCode, DimensionType.DEVICE_TYPE);
//        return ResponseEntity.ok(stats);
//    }
//
//    @GetMapping("/{shortCode}/dimension/browser")
//    public ResponseEntity<ArrayList<Map<String, Object>>> getBrowserDimensionStats(@PathVariable String shortCode) {
//        log.info("AnalyticsController.getBrowserDimensionStats: Getting browser dimension stats for short code: {}", shortCode);
//        ArrayList<Map<String, Object>> stats = analyticService.getDimensionStats(shortCode, DimensionType.BROWSER);
//        return ResponseEntity.ok(stats);
//    }
//
//    @GetMapping("/{shortCode}/dimension/os")
//    public ResponseEntity<ArrayList<Map<String, Object>>> getOsDimensionStats(@PathVariable String shortCode) {
//        log.info("AnalyticsController.getOsDimensionStats: Getting OS dimension stats for short code: {}", shortCode);
//        ArrayList<Map<String, Object>> stats = analyticService.getDimensionStats(shortCode, DimensionType.OS);
//        return ResponseEntity.ok(stats);
//    }
//
//    @GetMapping("/{shortCode}/dimension/country")
//    public ResponseEntity<ArrayList<Map<String, Object>>> getCountryDimensionStats(@PathVariable String shortCode) {
//        log.info("AnalyticsController.getCountryDimensionStats: Getting country dimension stats for short code: {}", shortCode);
//        ArrayList<Map<String, Object>> stats = analyticService.getDimensionStats(shortCode, DimensionType.COUNTRY);
//        return ResponseEntity.ok(stats);
//    }
//
//    @GetMapping("/{shortCode}/dimension/city")
//    public ResponseEntity<ArrayList<Map<String, Object>>> getCityDimensionStats(@PathVariable String shortCode) {
//        log.info("AnalyticsController.getCityDimensionStats: Getting city dimension stats for short code: {}", shortCode);
//        ArrayList<Map<String, Object>> stats = analyticService.getDimensionStats(shortCode, DimensionType.CITY);
//        return ResponseEntity.ok(stats);
//    }
//
//    /**
//     * Get list of all supported dimension types and their aliases
//     */
//    @GetMapping("/dimension/types")
//    public ResponseEntity<Map<String, Object>> getSupportedDimensionTypes() {
//        log.info("AnalyticsController.getSupportedDimensionTypes: Getting supported dimension types");
//
//        // Create supported types map
//        Map<String, String> supportedTypes = new HashMap<>();
//        supportedTypes.put("device", "Device related dimensions");
//        supportedTypes.put("browser", "Browser information");
//        supportedTypes.put("os", "Operating system information");
//        supportedTypes.put("country", "Country/location information");
//        supportedTypes.put("city", "City information");
//        supportedTypes.put("region", "Region information");
//        supportedTypes.put("timezone", "Timezone information");
//        supportedTypes.put("referrer", "Referrer information");
//        supportedTypes.put("utm_source", "UTM source tracking");
//        supportedTypes.put("utm_medium", "UTM medium tracking");
//        supportedTypes.put("utm_campaign", "UTM campaign tracking");
//        supportedTypes.put("isp", "Internet Service Provider");
//        supportedTypes.put("language", "User language");
//        supportedTypes.put("custom", "Custom dimensions");
//
//        // Create aliases map
//        Map<String, String> aliases = new HashMap<>();
//        aliases.put("device_type", "device");
//        aliases.put("devicetype", "device");
//        aliases.put("operating_system", "os");
//        aliases.put("operatingsystem", "os");
//        aliases.put("location", "country");
//        aliases.put("referrer_type", "referrer_type");
//        aliases.put("utmsource", "utm_source");
//        aliases.put("utmmedium", "utm_medium");
//
//        // Create examples list
//        List<String> examples = Arrays.asList(
//            "/api/v1/analytics/{shortCode}/dimension?type=device",
//            "/api/v1/analytics/{shortCode}/dimension?type=browser",
//            "/api/v1/analytics/{shortCode}/dimension?type=country",
//            "/api/v1/analytics/{shortCode}/dimension?type=utm_source"
//        );
//
//        // Build response
//        Map<String, Object> response = new HashMap<>();
//        response.put("supportedTypes", supportedTypes);
//        response.put("aliases", aliases);
//        response.put("examples", examples);
//
//        return ResponseEntity.ok(response);
//    }

    @GetMapping("/{shortCode}/overview")
    public ResponseEntity<Map<String, Object>> getOverviewStats(@PathVariable String shortCode) {
        log.info("AnalyticsController.getOverviewStats: Getting overview stats for short code: {}", shortCode);
        Map<String, Object> overview = new HashMap<>();
        overview.put("device", analyticService.getDeviceStats(shortCode));
        overview.put("location", analyticService.getLocationStats(shortCode));
        overview.put("time", analyticService.getTimeStats(shortCode));
        return ResponseEntity.ok(overview);
    }
}
