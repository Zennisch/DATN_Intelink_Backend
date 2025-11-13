// java
package intelink.controllers;

import intelink.dto.response.stat.AggregateByCountryResponse;
import intelink.dto.response.stat.AggregateByDimensionResponse;
import intelink.dto.response.stat.TimeSeriesAggregateResponse;
import intelink.models.enums.DimensionType;
import intelink.services.AggregateStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/statistics")
@RequiredArgsConstructor
@Slf4j
public class AggregateStatisticsController {

    private final AggregateStatisticsService aggregateService;

    @GetMapping("/by-country")
    public ResponseEntity<AggregateByCountryResponse> byCountry(
            @RequestParam(required = false) String shortCodes,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "10") Integer limit
    ) {
        log.info("by-country shortCodes={}, from={}, to={}, limit={}", shortCodes, from, to, limit);
        AggregateByCountryResponse result = aggregateService.getByCountry(shortCodes, from, to, limit);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/timeseries")
    public ResponseEntity<TimeSeriesAggregateResponse> timeSeries(
            @RequestParam(required = false) String shortCodes,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "DAILY") String granularity
    ) {
        log.info("timeseries shortCodes={}, from={}, to={}, granularity={}", shortCodes, from, to, granularity);
        TimeSeriesAggregateResponse result = aggregateService.getTimeSeries(shortCodes, from, to, granularity);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/by-dimension")
    public ResponseEntity<AggregateByDimensionResponse> byDimension(
            @RequestParam(required = false) String shortCodes,
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to,
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            @RequestParam(required = false, defaultValue = "COUNTRY") String dimension
    ) {
        log.info("by-dimension shortCodes={}, from={}, to={}, limit={}, dimension={}", shortCodes, from, to, limit, dimension);
        DimensionType type;
        try {
            type = DimensionType.valueOf(dimension.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        AggregateByDimensionResponse result = aggregateService.getByDimension(shortCodes, from, to, limit, type);
        return ResponseEntity.ok(result);
    }
}