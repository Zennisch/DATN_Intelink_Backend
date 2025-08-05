package intelink.services;

import intelink.models.DailyStat;
import intelink.models.DimensionStat;
import intelink.models.HourlyStat;
import intelink.models.enums.DimensionType;
import intelink.repositories.DailyStatRepository;
import intelink.repositories.DimensionStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final DailyStatRepository dailyStatRepository;
    private final DimensionStatRepository dimensionStatRepository;
    private final ClickLogService clickLogService;

    @Transactional
    public void updateStatsForClick(String shortCode, LocalDate date, int hour, Map<DimensionType, String> dimensions) {
        DailyStat dailyStat = findOrCreateDailyStat(shortCode, date);
        dailyStat.incrementClicksForHour(hour);
        dimensions.forEach((type, value) -> {
            if (value != null && !value.isEmpty()) {
                updateDimensionStat(shortCode, date, type, value);
            }
        });
        dailyStatRepository.save(dailyStat);
    }

    private DailyStat findOrCreateDailyStat(String shortCode, LocalDate date) {
        return dailyStatRepository.findByShortCodeAndDate(shortCode, date)
                .orElseGet(() -> {
                    log.info("Creating new DailyStat for {} on {}", shortCode, date);
                    DailyStat newStat = DailyStat.builder()
                            .shortCode(shortCode)
                            .date(date)
                            .clickCount(0L)
                            .build();

                    for (int hour = 0; hour < 24; hour++) {
                        HourlyStat hourlyStat = HourlyStat.builder()
                                .hour(hour)
                                .clickCount(0L)
                                .dailyStat(newStat)
                                .build();
                        newStat.getHourlyStats().add(hourlyStat);
                    }

                    return newStat;
                });
    }

    @Transactional
    public void updateDimensionStat(String shortCode, LocalDate date, DimensionType type, String value) {
        DimensionStat dimensionStat = dimensionStatRepository
                .findByShortCodeAndDateAndTypeAndValue(shortCode, date, type, value)
                .orElseGet(() -> {
                    log.info("Creating new DimensionStat for {} on {}: {} - {}", shortCode, date, type, value);
                    return DimensionStat.builder()
                            .shortCode(shortCode)
                            .date(date)
                            .type(type)
                            .value(value)
                            .clickCount(0L)
                            .build();
                });

        dimensionStat.setClickCount(dimensionStat.getClickCount() + 1);
        dimensionStatRepository.save(dimensionStat);
    }

    @Async
    @Transactional
    public void recalculateStatsForDate(String shortCode, LocalDate date) {
        log.info("Recalculating stats for {} on {}", shortCode, date);

        dailyStatRepository.findByShortCodeAndDate(shortCode, date)
                .ifPresent(dailyStatRepository::delete);

        List<DimensionStat> oldDimensionStats = dimensionStatRepository.findByShortCodeAndDate(shortCode, date);
        dimensionStatRepository.deleteAll(oldDimensionStats);

        int[] hourlyClicksArray = clickLogService.getHourlyClicksForDate(shortCode, date);
        long totalClicks = 0;

        for (int clicks : hourlyClicksArray) {
            totalClicks += clicks;
        }

        if (totalClicks > 0) {
            DailyStat newDailyStat = DailyStat.builder()
                    .shortCode(shortCode)
                    .date(date)
                    .clickCount(totalClicks)
                    .build();

            for (int hour = 0; hour < hourlyClicksArray.length; hour++) {
                if (hourlyClicksArray[hour] > 0) {
                    HourlyStat hourlyStat = HourlyStat.builder()
                            .hour(hour)
                            .clickCount((long) hourlyClicksArray[hour])
                            .dailyStat(newDailyStat)
                            .build();
                    newDailyStat.getHourlyStats().add(hourlyStat);
                }
            }

            dailyStatRepository.save(newDailyStat);

            recalculateDimensionStats(shortCode, date);
        }

        log.info("Completed recalculation for {} on {}", shortCode, date);
    }

    private void recalculateDimensionStats(String shortCode, LocalDate date) {
        Map<String, Long> countryStats = clickLogService.getCountryStatistics(shortCode);
        Map<String, Long> browserStats = clickLogService.getBrowserStatistics(shortCode);
        Map<String, Long> deviceStats = clickLogService.getDeviceTypeStatistics(shortCode);

        // Save country stats
        countryStats.forEach((country, count) -> {
            if (country != null) {
                updateDimensionStat(shortCode, date, DimensionType.COUNTRY, country);
            }
        });

        // Save browser stats
        browserStats.forEach((browser, count) -> {
            if (browser != null) {
                updateDimensionStat(shortCode, date, DimensionType.BROWSER, browser);
            }
        });

        // Save device stats
        deviceStats.forEach((device, count) -> {
            if (device != null) {
                updateDimensionStat(shortCode, date, DimensionType.DEVICE_TYPE, device);
            }
        });
    }

    // Other methods remain unchanged
    @Transactional(readOnly = true)
    public List<DailyStat> getDailyStatsForRange(String shortCode, LocalDate startDate, LocalDate endDate) {
        return dailyStatRepository.findByShortCodeAndDateBetween(shortCode, startDate, endDate);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getTopValuesByDimension(String shortCode, DimensionType type) {
        List<Object[]> results = dimensionStatRepository.getTopValuesByType(shortCode, type);
        return results.stream()
                .collect(java.util.stream.Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1],
                        (existing, replacement) -> existing,
                        java.util.LinkedHashMap::new
                ));
    }

}