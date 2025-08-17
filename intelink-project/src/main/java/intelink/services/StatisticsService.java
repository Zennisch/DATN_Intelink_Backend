package intelink.services;

import intelink.dto.object.StatEntry;
import intelink.dto.object.StatsCategory;
import intelink.dto.response.StatisticsResponse;
import intelink.dto.response.TimeStatsResponse;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    public TimeStatsResponse getTimeStats(String shortCode, String customFrom, String customTo) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("StatisticsService.getTimeStats: Short code not found: " + shortCode));

        Instant now = Instant.now();

        // Nếu có customFrom và customTo, trả về một danh mục duy nhất
        if (customFrom != null && customTo != null) {
            Instant fromInstant = Instant.parse(customFrom);
            Instant toInstant = Instant.parse(customTo);

            // Lấy dữ liệu thống kê với granularity HOURLY để chi tiết nhất
            List<ClickStat> stats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualAndBucketLessThanOrderByBucketAsc(
                    shortUrl, Granularity.HOURLY, fromInstant, toInstant);

            // Tính tổng số lượt click
            long totalClicks = stats.stream().mapToLong(ClickStat::getTotalClicks).sum();

            // Tạo danh sách StatEntry với định dạng time và clicks
            List<StatEntry> statEntries = stats.stream()
                    .map(stat -> new StatEntry(stat.getBucket().toString(), stat.getTotalClicks()))
                    .collect(Collectors.toList());

            // Tạo category với tên là khoảng thời gian
            String categoryName = fromInstant.toString() + " to " + toInstant.toString();
            StatsCategory category = new StatsCategory(categoryName, totalClicks, statEntries);

            // Trả về TimeStatsResponse với một category duy nhất
            return new TimeStatsResponse(category);
        }

        // Nếu không có customFrom hoặc customTo, trả về hourly, daily, monthly
        Instant to = now;

        // Hourly stats
        Instant fromHourly = to.minus(24, ChronoUnit.HOURS);
        List<ClickStat> hourlyStats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualAndBucketLessThanOrderByBucketAsc(
                shortUrl, Granularity.HOURLY, fromHourly, to);
        long totalHourlyClicks = hourlyStats.stream().mapToLong(ClickStat::getTotalClicks).sum();
        StatsCategory hourly = new StatsCategory(
                "Hourly (Custom or Last 24h)",
                totalHourlyClicks,
                hourlyStats.stream()
                        .map(stat -> new StatEntry(stat.getBucket().toString(), stat.getTotalClicks()))
                        .collect(Collectors.toList())
        );

        // Daily stats
        Instant fromDaily = to.minus(30, ChronoUnit.DAYS);
        List<ClickStat> dailyStats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualAndBucketLessThanOrderByBucketAsc(
                shortUrl, Granularity.DAILY, fromDaily, to);
        long totalDailyClicks = dailyStats.stream().mapToLong(ClickStat::getTotalClicks).sum();
        StatsCategory daily = new StatsCategory(
                "Daily (Custom or Last 30 days)",
                totalDailyClicks,
                dailyStats.stream()
                        .map(stat -> new StatEntry(stat.getBucket().toString(), stat.getTotalClicks()))
                        .collect(Collectors.toList())
        );

        // Monthly stats
        Instant fromMonthly = to.minus(365, ChronoUnit.DAYS);
        List<ClickStat> monthlyStats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualAndBucketLessThanOrderByBucketAsc(
                shortUrl, Granularity.MONTHLY, fromMonthly, to);
        long totalMonthlyClicks = monthlyStats.stream().mapToLong(ClickStat::getTotalClicks).sum();
        StatsCategory monthly = new StatsCategory(
                "Monthly (Custom or Last 12 months)",
                totalMonthlyClicks,
                monthlyStats.stream()
                        .map(stat -> new StatEntry(stat.getBucket().toString(), stat.getTotalClicks()))
                        .collect(Collectors.toList())
        );

        return new TimeStatsResponse(hourly, daily, monthly);
    }

    @Override
    public StatisticsResponse getDimensionStats(String shortCode, String type) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("StatisticsService.getDimensionStats: Short code not found: " + shortCode));

        DimensionType dimensionType;
        try {
            // First try direct enum matching
            dimensionType = DimensionType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            // If direct matching fails, try mapping common aliases
            dimensionType = mapStringToDimensionType(type.toLowerCase());
        }

        List<DimensionStat> stats = dimensionStatRepository.findByShortUrlAndTypeOrderByTotalClicksDesc(shortUrl, dimensionType);
        long totalClicks = stats.stream().mapToLong(DimensionStat::getTotalClicks).sum();

        List<StatisticsResponse.StatData> statDataList = new ArrayList<>();
        for (DimensionStat stat : stats) {
            statDataList.add(StatisticsResponse.StatData.builder()
                    .name(stat.getValue())
                    .clicks(stat.getTotalClicks())
                    .percentage(totalClicks > 0 ? Math.round((double) stat.getTotalClicks() / totalClicks * 100.0 * 100.0) / 100.0 : 0.0)
                    .build());
        }

        return StatisticsResponse.builder()
                .shortCode(shortCode)
                .category(type)
                .totalClicks(totalClicks)
                .data(statDataList)
                .build();
    }

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
}
