package intelink.services;

import intelink.dto.helper.DimensionInfo;
import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.repositories.ClickStatRepository;
import intelink.repositories.DimensionStatRepository;
import intelink.services.interfaces.IAnalyticService;
import intelink.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticService implements IAnalyticService {

    private final DimensionStatRepository dimensionStatRepository;
    private final ClickStatRepository clickStatRepository;
    private final ShortUrlService shortUrlService;

    private static <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    @Transactional(readOnly = true)
    public void recordDimensionStats(String shortCode, DimensionInfo dimensionInfo) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("AnalyticsService.recordDimensionStats: Short code not found: " + shortCode));

        Map<DimensionType, String> dimensions = Stream.of(
                        entry(DimensionType.COUNTRY, dimensionInfo.getCountry()),
                        entry(DimensionType.CITY, dimensionInfo.getCity()),
                        entry(DimensionType.BROWSER, dimensionInfo.getBrowser()),
                        entry(DimensionType.OS, dimensionInfo.getOs()),
                        entry(DimensionType.DEVICE_TYPE, dimensionInfo.getDeviceType())
                )
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        dimensions.forEach((type, value) -> {
            if (value == null || value.isBlank()) {
                log.warn("AnalyticsService.recordDimensionStats: Dimension {} is null or blank for short code {}", type, shortCode);
                return;
            }
            DimensionStat dimensionStat = dimensionStatRepository
                    .findByShortUrlAndTypeAndValue(shortUrl, type, value)
                    .orElseGet(() -> {
                        log.info("Creating new DimensionStat for short code: {}, type: {}, value: {}", shortCode, type, value);
                        return DimensionStat.builder()
                                .shortUrl(shortUrl)
                                .type(type)
                                .value(value)
                                .totalClicks(0L)
                                .build();
                    });
            dimensionStat.setTotalClicks(dimensionStat.getTotalClicks() + 1);
            dimensionStatRepository.save(dimensionStat);
        });
    }

    @Transactional
    public void recordClickStats(String shortCode) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("AnalyticsService.recordClickStats: Short code not found: " + shortCode));

        Instant now = Instant.now();
        for (Granularity granularity : Granularity.values()) {
            Instant bucket = DateTimeUtil.getBucketStart(now, granularity);
            ClickStat clickStat = clickStatRepository
                    .findByShortUrlAndGranularityAndBucket(shortUrl, granularity, bucket)
                    .orElseGet(() -> {
                        log.info("Creating new ClickStat for short code: {}, granularity: {}, bucket: {}", shortCode, granularity, bucket);
                        return ClickStat.builder()
                                .shortUrl(shortUrl)
                                .granularity(granularity)
                                .bucket(bucket)
                                .totalClicks(0L)
                                .build();
                    });
            clickStat.setTotalClicks(clickStat.getTotalClicks() + 1);
            clickStatRepository.save(clickStat);
        }
    }

    @Override
    public ArrayList<Map<String, Object>> getDeviceStats(String shortCode) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("AnalyticsService.getDeviceStats: Short code not found: " + shortCode));

        // Lấy thống kê theo browser, OS, device type
        ArrayList<Map<String, Object>> result = new ArrayList<>();
        
        // Browser stats
        List<DimensionStat> browserStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.BROWSER);
        long totalBrowserClicks = browserStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        Map<String, Object> browserData = Map.of(
                "category", "Browser",
                "totalClicks", totalBrowserClicks,
                "data", browserStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalBrowserClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalBrowserClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        );
        result.add(browserData);

        // OS stats
        List<DimensionStat> osStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.OS);
        long totalOsClicks = osStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        Map<String, Object> osData = Map.of(
                "category", "Operating System",
                "totalClicks", totalOsClicks,
                "data", osStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalOsClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalOsClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        );
        result.add(osData);

        // Device Type stats
        List<DimensionStat> deviceStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.DEVICE_TYPE);
        long totalDeviceClicks = deviceStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        Map<String, Object> deviceData = Map.of(
                "category", "Device Type",
                "totalClicks", totalDeviceClicks,
                "data", deviceStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalDeviceClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalDeviceClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        );
        result.add(deviceData);

        return result;
    }

    @Override
    public ArrayList<Map<String, Object>> getLocationStats(String shortCode) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("AnalyticsService.getLocationStats: Short code not found: " + shortCode));

        ArrayList<Map<String, Object>> result = new ArrayList<>();

        // Country stats
        List<DimensionStat> countryStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.COUNTRY);
        long totalCountryClicks = countryStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        Map<String, Object> countryData = Map.of(
                "category", "Country",
                "totalClicks", totalCountryClicks,
                "data", countryStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalCountryClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalCountryClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        );
        result.add(countryData);

        // City stats
        List<DimensionStat> cityStats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, DimensionType.CITY);
        long totalCityClicks = cityStats.stream().mapToLong(DimensionStat::getTotalClicks).sum();
        Map<String, Object> cityData = Map.of(
                "category", "City",
                "totalClicks", totalCityClicks,
                "data", cityStats.stream().map(stat -> Map.of(
                        "name", stat.getValue(),
                        "clicks", stat.getTotalClicks(),
                        "percentage", totalCityClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalCityClicks * 100.0 * 100.0) / 100.0 : 0.0
                )).toList()
        );
        result.add(cityData);

        return result;
    }

    @Override
    public ArrayList<Map<String, Object>> getTimeStats(String shortCode) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("AnalyticsService.getTimeStats: Short code not found: " + shortCode));

        ArrayList<Map<String, Object>> result = new ArrayList<>();

        // Hourly stats (last 24 hours)
        List<ClickStat> hourlyStats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualOrderByBucketAsc(
                shortUrl, Granularity.HOURLY, Instant.now().minusSeconds(24 * 60 * 60));
        long totalHourlyClicks = hourlyStats.stream().mapToLong(ClickStat::getTotalClicks).sum();
        Map<String, Object> hourlyData = Map.of(
                "category", "Hourly (Last 24h)",
                "totalClicks", totalHourlyClicks,
                "data", hourlyStats.stream().map(stat -> Map.of(
                        "time", stat.getBucket().toString(),
                        "clicks", stat.getTotalClicks()
                )).toList()
        );
        result.add(hourlyData);

        // Daily stats (last 30 days)
        List<ClickStat> dailyStats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualOrderByBucketAsc(
                shortUrl, Granularity.DAILY, Instant.now().minusSeconds(30L * 24 * 60 * 60));
        long totalDailyClicks = dailyStats.stream().mapToLong(ClickStat::getTotalClicks).sum();
        Map<String, Object> dailyData = Map.of(
                "category", "Daily (Last 30 days)",
                "totalClicks", totalDailyClicks,
                "data", dailyStats.stream().map(stat -> Map.of(
                        "time", stat.getBucket().toString(),
                        "clicks", stat.getTotalClicks()
                )).toList()
        );
        result.add(dailyData);

        // Monthly stats (last 12 months)
        List<ClickStat> monthlyStats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualOrderByBucketAsc(
                shortUrl, Granularity.MONTHLY, Instant.now().minusSeconds(365L * 24 * 60 * 60));
        long totalMonthlyClicks = monthlyStats.stream().mapToLong(ClickStat::getTotalClicks).sum();
        Map<String, Object> monthlyData = Map.of(
                "category", "Monthly (Last 12 months)",
                "totalClicks", totalMonthlyClicks,
                "data", monthlyStats.stream().map(stat -> Map.of(
                        "time", stat.getBucket().toString(),
                        "clicks", stat.getTotalClicks()
                )).toList()
        );
        result.add(monthlyData);

        return result;
    }

    @Override
    public ArrayList<Map<String, Object>> getDimensionStats(String shortCode, DimensionType type) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("AnalyticsService.getDimensionStats: Short code not found: " + shortCode));

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
        result.add(data);

        return result;
    }
}
