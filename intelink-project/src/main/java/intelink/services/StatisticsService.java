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
        switch (granularity) {
            case HOURLY -> {
                to = now.truncatedTo(ChronoUnit.HOURS);
                from = to.minus(23, ChronoUnit.HOURS); // 24 buckets
                bucketCount = 24;
            }
            case DAILY -> {
                to = now.truncatedTo(ChronoUnit.DAYS);
                from = to.minus(29, ChronoUnit.DAYS); // 30 buckets
                bucketCount = 30;
            }
            case MONTHLY -> {
                ZonedDateTime zdt = now.atZone(ZoneId.systemDefault()).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                to = zdt.toInstant();
                from = zdt.minusMonths(11).toInstant(); // 12 buckets
                bucketCount = 12;
            }
            case YEARLY -> {
                ZonedDateTime zdt = now.atZone(ZoneId.systemDefault()).withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
                to = zdt.toInstant();
                from = zdt.minusYears(9).toInstant(); // 10 buckets, có thể thay đổi
                bucketCount = 10;
            }
            default -> throw new IllegalArgumentException("Unsupported granularity");
        }
        ZoneId zoneId = ZoneId.systemDefault();
        if (customFrom != null && customTo != null) {
            from = Instant.parse(customFrom);
            to = Instant.parse(customTo);
            if (granularity == Granularity.MONTHLY) {
                // Convert to ZonedDateTime for monthly granularity
                ZonedDateTime zdtFrom = from.atZone(ZoneId.systemDefault());
                ZonedDateTime zdtTo = to.atZone(ZoneId.systemDefault());
                bucketCount = (int) ChronoUnit.MONTHS.between(zdtFrom, zdtTo) + 1;
            } else if (granularity == Granularity.YEARLY) {
                // Convert to ZonedDateTime for yearly granularity
                ZonedDateTime zdtFrom = from.atZone(ZoneId.systemDefault());
                ZonedDateTime zdtTo = to.atZone(ZoneId.systemDefault());
                bucketCount = (int) ChronoUnit.YEARS.between(zdtFrom, zdtTo) + 1;
            } else {
                ChronoUnit unit = getChronoUnit(granularity);
                bucketCount = (int) unit.between(from, to) + 1;
            }
        }

        List<ClickStat> stats = clickStatRepository.findByShortUrlAndGranularityAndBucketGreaterThanEqualAndBucketLessThanOrderByBucketAsc(
                shortUrl, granularity, from, to
        );
        // Map bucket -> clicks
        Map<Instant, Long> bucketClicks = stats.stream()
                .collect(Collectors.toMap(ClickStat::getBucket, ClickStat::getTotalClicks));

        // Tạo danh sách StatEntry đủ bucket
        List<TimeStatsResponse.Bucket> buckets = new ArrayList<>();
        Instant bucket = from;

        for (int i = 0; i < bucketCount; i++) {
            long clicks = bucketClicks.getOrDefault(bucket, 0L);
            buckets.add(new TimeStatsResponse.Bucket(bucket.toString(), clicks));
            switch (granularity) {
                case HOURLY -> bucket = bucket.plus(1, ChronoUnit.HOURS);
                case DAILY -> bucket = bucket.plus(1, ChronoUnit.DAYS);
                case MONTHLY -> {
                    ZonedDateTime zdt = bucket.atZone(zoneId).plusMonths(1);
                    bucket = zdt.toInstant();
                }
                case YEARLY -> {
                    ZonedDateTime zdt = bucket.atZone(zoneId).plusYears(1);
                    bucket = zdt.toInstant();
                }
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


}