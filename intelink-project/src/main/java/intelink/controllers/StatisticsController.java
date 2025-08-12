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
            DimensionType dimensionType = DimensionType.fromString(type);

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

