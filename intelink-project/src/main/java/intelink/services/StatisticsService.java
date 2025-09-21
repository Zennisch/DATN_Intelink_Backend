package intelink.services;

import intelink.dto.response.stat.StatisticsResponse;
import intelink.dto.response.stat.TimeStatsResponse;
import intelink.dto.response.stat.TopPeakTimesResponse;
import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.repositories.ClickStatRepository;
import intelink.repositories.DimensionStatRepository;
import intelink.services.interfaces.IStatisticsService;
import intelink.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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

    private ChronoUnit getChronoUnit(Granularity granularity) {
        return switch (granularity) {
            case HOURLY -> ChronoUnit.HOURS;
            case DAILY -> ChronoUnit.DAYS;
            case MONTHLY -> ChronoUnit.MONTHS;
            case YEARLY -> ChronoUnit.YEARS;
        };
    }

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
    public TimeStatsResponse getTimeStats(String shortCode, String customFrom, String customTo, String granularityStr) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("StatisticsService.getTimeStats: Short code not found: " + shortCode));

        Granularity granularity = granularityStr != null ? Granularity.fromString(granularityStr) : Granularity.HOURLY;
        Instant now = Instant.now();

        Instant from, to;
        int bucketCount;

        // Xử lý mặc định nếu không truyền customFrom/customTo - sử dụng UTC
        switch (granularity) {
            case HOURLY -> {
                to = DateTimeUtil.getBucketStart(now, granularity);
                from = to.minus(23, ChronoUnit.HOURS);
                bucketCount = 24;
            }
            case DAILY -> {
                to = DateTimeUtil.getBucketStart(now, granularity);
                from = to.minus(29, ChronoUnit.DAYS);
                bucketCount = 30;
            }
            case MONTHLY -> {
                to = DateTimeUtil.getBucketStart(now, granularity);
                ZonedDateTime zdt = to.atZone(ZoneId.of("UTC"));
                from = zdt.minusMonths(11).toInstant();
                bucketCount = 12;
            }
            case YEARLY -> {
                to = DateTimeUtil.getBucketStart(now, granularity);
                ZonedDateTime zdt = to.atZone(ZoneId.of("UTC"));
                from = zdt.minusYears(9).toInstant();
                bucketCount = 10;
            }
            default -> throw new IllegalArgumentException("Unsupported granularity");
        }

        // Nếu truyền customFrom/customTo thì tính lại from, to, bucketCount
        if (customFrom != null && customTo != null) {
            from = Instant.parse(customFrom);
            to = Instant.parse(customTo);

            from = DateTimeUtil.getBucketStart(from, granularity);
            to = DateTimeUtil.getBucketStart(to, granularity);

            ZoneId zoneId = ZoneId.of("UTC");
            switch (granularity) {
                case HOURLY -> bucketCount = (int) ChronoUnit.HOURS.between(from, to) + 1;
                case DAILY -> bucketCount = (int) ChronoUnit.DAYS.between(from, to) + 1;
                case MONTHLY -> {
                    ZonedDateTime zdtFrom = from.atZone(zoneId);
                    ZonedDateTime zdtTo = to.atZone(zoneId);
                    bucketCount = (int) ChronoUnit.MONTHS.between(zdtFrom, zdtTo) + 1;
                }
                case YEARLY -> {
                    ZonedDateTime zdtFrom = from.atZone(zoneId);
                    ZonedDateTime zdtTo = to.atZone(zoneId);
                    bucketCount = (int) ChronoUnit.YEARS.between(zdtFrom, zdtTo) + 1;
                }
            }
        }

        // Truy vấn dữ liệu
        List<ClickStat> stats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualAndBucketLessThanEqualOrderByBucketAsc(
                shortUrl, granularity, from, to
        );

        // Map bucket -> clicks
        Map<Instant, Long> bucketClicks = stats.stream()
                .collect(Collectors.toMap(
                        ClickStat::getBucket,
                        ClickStat::getTotalClicks,
                        Long::sum
                ));

        // Sinh danh sách bucket đủ số lượng
        List<TimeStatsResponse.Bucket> buckets = new ArrayList<>();
        Instant bucket = DateTimeUtil.getBucketStart(from, granularity);
        ZoneId zoneId = ZoneId.of("UTC");

        for (int i = 0; i < bucketCount; i++) {
            long clicks = bucketClicks.getOrDefault(bucket, 0L);
            buckets.add(new TimeStatsResponse.Bucket(bucket.toString(), clicks));

            // Tăng bucket lên theo granularity - sử dụng calendar arithmetic
            switch (granularity) {
                case HOURLY -> bucket = bucket.plus(1, ChronoUnit.HOURS);
                case DAILY -> bucket = bucket.plus(1, ChronoUnit.DAYS);
                case MONTHLY -> bucket = bucket.atZone(zoneId).plusMonths(1).toInstant();
                case YEARLY -> bucket = bucket.atZone(zoneId).plusYears(1).toInstant();
            }
        }

        long totalClicks = buckets.stream().mapToLong(TimeStatsResponse.Bucket::getClicks).sum();
        return new TimeStatsResponse(granularity.name(), from.toString(), to.toString(), totalClicks, buckets);
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

    /**
     * Trả về bucket (thời gian) có số lượt click nhiều nhất theo granularity.
     */
    @Override
    public Map<String, Object> getPeakTimeStats(String shortCode, String customFrom, String customTo, String granularityStr) {
        TimeStatsResponse stats = getTimeStats(shortCode, customFrom, customTo, granularityStr);
        if (stats.getBuckets() == null || stats.getBuckets().isEmpty()) {
            return Map.of(
                    "peakTime", null,
                    "clicks", 0,
                    "granularity", stats.getGranularity()
            );
        }
        // Tìm bucket có số click lớn nhất
        TimeStatsResponse.Bucket peak = stats.getBuckets().stream()
                .max((b1, b2) -> Long.compare(b1.getClicks(), b2.getClicks()))
                .orElse(null);

        return Map.of(
                "peakTime", peak != null ? peak.getTime() : null,
                "clicks", peak != null ? peak.getClicks() : 0,
                "granularity", stats.getGranularity()
        );
    }

    @Override
    public TopPeakTimesResponse getTopPeakTimes(String shortCode, String granularityStr) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("Short code not found: " + shortCode));
        Granularity granularity = granularityStr != null ? Granularity.fromString(granularityStr) : Granularity.HOURLY;

        List<ClickStat> stats = clickStatRepository.findByShortUrlAndGranularityOrderByBucketAsc(shortUrl, granularity);

        List<TopPeakTimesResponse.PeakTime> topList = stats.stream()
                .sorted((a, b) -> Long.compare(b.getTotalClicks(), a.getTotalClicks()))
                .limit(10)
                .map(stat -> new TopPeakTimesResponse.PeakTime(
                        stat.getBucket().toString(),
                        stat.getTotalClicks()
                ))
                .toList();

        return new TopPeakTimesResponse(
                granularity.name(),
                topList.size(),
                topList
        );
    }

}