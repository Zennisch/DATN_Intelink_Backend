// java
package intelink.controllers;

import intelink.dto.response.stat.AggregateByCountryResponse;
import intelink.dto.response.stat.TimeSeriesAggregateResponse;
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
}