package intelink.services;

import intelink.models.DailyStat;
import intelink.models.DimensionStat;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticsService {

    private final DailyStatRepository dailyStatRepository;
    private final DimensionStatRepository dimensionStatRepository;
    private final ClickLogService clickLogService;

    @Transactional
    public void updateDailyStat(String shortCode, LocalDate date, int hour) {
        Optional<DailyStat> existingStat = dailyStatRepository.findByShortCodeAndDate(shortCode, date);

        if (existingStat.isPresent()) {
            DailyStat dailyStat = existingStat.get();
            dailyStat.setClickCount(dailyStat.getClickCount() + 1);

            int[] hourlyClicks = dailyStat.getHourlyClicks();
            hourlyClicks[hour]++;
            dailyStat.setHourlyClicks(hourlyClicks);

            dailyStatRepository.save(dailyStat);
        } else {
            int[] hourlyClicks = new int[24];
            hourlyClicks[hour] = 1;

            DailyStat newStat = DailyStat.builder()
                    .shortCode(shortCode)
                    .date(date)
                    .clickCount(1L)
                    .build();
            newStat.setHourlyClicks(hourlyClicks);

            dailyStatRepository.save(newStat);
        }
    }

    @Transactional
    public void updateDimensionStat(String shortCode, LocalDate date, DimensionType type, String value) {
        String id = shortCode + "_" + date + "_" + type + "_" + value.hashCode();

        Optional<DimensionStat> existingStat = dimensionStatRepository.findById(id);

        if (existingStat.isPresent()) {
            DimensionStat dimensionStat = existingStat.get();
            dimensionStat.setClickCount(dimensionStat.getClickCount() + 1);
            dimensionStatRepository.save(dimensionStat);
        } else {
            DimensionStat newStat = DimensionStat.builder()
                    .shortCode(shortCode)
                    .date(date)
                    .type(type)
                    .value(value)
                    .clickCount(1L)
                    .build();

            dimensionStatRepository.save(newStat);
        }
    }

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

    @Async
    @Transactional
    public void recalculateStatsForDate(String shortCode, LocalDate date) {
        log.info("Recalculating stats for {} on {}", shortCode, date);

        // Xóa stats cũ
        dailyStatRepository.findByShortCodeAndDate(shortCode, date)
                .ifPresent(dailyStatRepository::delete);

        List<DimensionStat> oldDimensionStats = dimensionStatRepository.findByShortCodeAndDate(shortCode, date);
        dimensionStatRepository.deleteAll(oldDimensionStats);

        // Tính toán lại từ ClickLog
        int[] hourlyClicks = clickLogService.getHourlyClicksForDate(shortCode, date);
        long totalClicks = java.util.Arrays.stream(hourlyClicks).sum();

        if (totalClicks > 0) {
            DailyStat newDailyStat = DailyStat.builder()
                    .shortCode(shortCode)
                    .date(date)
                    .clickCount(totalClicks)
                    .build();
            newDailyStat.setHourlyClicks(hourlyClicks);

            dailyStatRepository.save(newDailyStat);

            // Tính toán dimension stats
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
}