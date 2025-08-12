package intelink.services;

import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.repositories.ClickStatRepository;
import intelink.repositories.DimensionStatRepository;
import intelink.services.interfaces.IStatisticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService implements IStatisticsService {

    private final DimensionStatRepository dimensionStatRepository;
    private final ClickStatRepository clickStatRepository;
    private final ShortUrlService shortUrlService;

    @Override
    public Map<String, Object> getDeviceStats(String shortCode) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("StatisticsService.getDeviceStats: Short code not found: " + shortCode));

        Map<String, Object> result = new HashMap<>();

        // Browser stats
        List<DimensionStat> browserStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.BROWSER);
        long totalBrowserClicks = browserStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        result.put("browser", Map.of(
                "category", "Browser",
                "totalClicks", totalBrowserClicks,
                "data", browserStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalBrowserClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalBrowserClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        ));

        // OS stats
        List<DimensionStat> osStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.OS);
        long totalOsClicks = osStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        result.put("os", Map.of(
                "category", "Operating System",
                "totalClicks", totalOsClicks,
                "data", osStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalOsClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalOsClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        ));

        // Device Type stats
        List<DimensionStat> deviceStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.DEVICE_TYPE);
        long totalDeviceClicks = deviceStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        result.put("deviceType", Map.of(
                "category", "Device Type",
                "totalClicks", totalDeviceClicks,
                "data", deviceStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalDeviceClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalDeviceClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        ));

        return result;
    }

    @Override
    public Map<String, Object> getLocationStats(String shortCode) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("StatisticsService.getLocationStats: Short code not found: " + shortCode));

        Map<String, Object> result = new HashMap<>();

        // Country stats
        List<DimensionStat> countryStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.COUNTRY);
        long totalCountryClicks = countryStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        result.put("country", Map.of(
                "category", "Country",
                "totalClicks", totalCountryClicks,
                "data", countryStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalCountryClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalCountryClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        ));

        // City stats
        List<DimensionStat> cityStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.CITY);
        long totalCityClicks = cityStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        result.put("city", Map.of(
                "category", "City",
                "totalClicks", totalCityClicks,
                "data", cityStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalCityClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalCityClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        ));

        return result;
    }

    @Override
    public Map<String, Object> getTimeStats(String shortCode) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("StatisticsService.getTimeStats: Short code not found: " + shortCode));

        Map<String, Object> result = new HashMap<>();

        // Hourly stats (last 24 hours)
        List<ClickStat> hourlyStats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualOrderByBucketAsc(
                shortUrl, Granularity.HOURLY, Instant.now().minusSeconds(24 * 60 * 60));
        long totalHourlyClicks = hourlyStats.stream().mapToLong(ClickStat::getTotalClicks).sum();
        result.put("hourly", Map.of(
                "category", "Hourly (Last 24h)",
                "totalClicks", totalHourlyClicks,
                "data", hourlyStats.stream().map(stat -> Map.of(
                        "time", stat.getBucket().toString(),
                        "clicks", stat.getTotalClicks()
                )).toList()
        ));

        // Daily stats (last 30 days)
        List<ClickStat> dailyStats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualOrderByBucketAsc(
                shortUrl, Granularity.DAILY, Instant.now().minusSeconds(30L * 24 * 60 * 60));
        long totalDailyClicks = dailyStats.stream().mapToLong(ClickStat::getTotalClicks).sum();
        result.put("daily", Map.of(
                "category", "Daily (Last 30 days)",
                "totalClicks", totalDailyClicks,
                "data", dailyStats.stream().map(stat -> Map.of(
                        "time", stat.getBucket().toString(),
                        "clicks", stat.getTotalClicks()
                )).toList()
        ));

        // Monthly stats (last 12 months)
        List<ClickStat> monthlyStats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualOrderByBucketAsc(
                shortUrl, Granularity.MONTHLY, Instant.now().minusSeconds(365L * 24 * 60 * 60));
        long totalMonthlyClicks = monthlyStats.stream().mapToLong(ClickStat::getTotalClicks).sum();
        result.put("monthly", Map.of(
                "category", "Monthly (Last 12 months)",
                "totalClicks", totalMonthlyClicks,
                "data", monthlyStats.stream().map(stat -> Map.of(
                        "time", stat.getBucket().toString(),
                        "clicks", stat.getTotalClicks()
                )).toList()
        ));

        return result;
    }

    @Override
    public Map<String, Object> getDimensionStats(String shortCode, DimensionType type) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("StatisticsService.getDimensionStats: Short code not found: " + shortCode));

        List<DimensionStat> stats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, type);
        long totalClicks = stats.stream().mapToLong(DimensionStat::getTotalClicks).sum();

        ArrayList<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> data = Map.of(
                "category", type.toString(),
                "totalClicks", totalClicks,
                "data", stats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        );


        return data;
    }
}
