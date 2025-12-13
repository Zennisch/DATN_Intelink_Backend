package intelink.modules.redirect.services;

import intelink.dto.statistics.*;
import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.modules.redirect.repositories.ClickLogRepository;
import intelink.modules.redirect.repositories.ClickStatRepository;
import intelink.modules.redirect.repositories.DimensionStatRepository;
import intelink.modules.url.services.ShortUrlService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StatisticsService {

    private final DimensionStatRepository dimensionStatRepository;
    private final ClickStatRepository clickStatRepository;
    private final ClickLogRepository clickLogRepository;
    private final ShortUrlService shortUrlService;
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Transactional(readOnly = true)
    public DimensionStatResponse getDimensionStats(User user, String shortCode, DimensionType dimensionType) {
        ShortUrl shortUrl = shortUrlService.getShortUrlByShortCode(user, shortCode);
        List<DimensionStat> stats = dimensionStatRepository.findByShortUrlAndTypeOrderByAllowedClicksDesc(shortUrl, dimensionType);
        return buildDimensionStatResponse(stats, dimensionType);
    }

    @Transactional(readOnly = true)
    public DimensionStatResponse getDimensionStats(User user, DimensionType dimensionType, String fromStr, String toStr, String timezoneStr) {
        if (fromStr == null && toStr == null) {
            return getDimensionStats(user, dimensionType);
        }

        TimeRange tr = parseTimeRange(Granularity.DAILY, fromStr, toStr, timezoneStr);
        
        // 1. Get total clicks in range from ClickStat
        List<ClickStat> clickStats = clickStatRepository.findByUserAndGranularityAndBucketStartBetween(
                user, tr.granularity, tr.fromUtc, tr.toUtc);
        long rangeTotalClicks = clickStats.stream().mapToLong(ClickStat::getAllowedClicks).sum();

        // 2. Get lifetime distribution from DimensionStat
        List<DimensionStat> lifetimeStats = dimensionStatRepository.findByUserAndTypeOrderByAllowedClicksDesc(user, dimensionType);
        long lifetimeTotalClicks = lifetimeStats.stream().mapToLong(DimensionStat::getAllowedClicks).sum();

        // 3. Estimate range stats based on lifetime distribution
        List<DimensionStatItemResponse> data = lifetimeStats.stream()
                .map(stat -> {
                    double percentage = lifetimeTotalClicks > 0 
                            ? (double) stat.getAllowedClicks() / lifetimeTotalClicks 
                            : 0.0;
                    long estimatedClicks = (long) (rangeTotalClicks * percentage);
                    
                    return DimensionStatItemResponse.builder()
                            .name(stat.getValue())
                            .clicks(estimatedClicks)
                            .percentage(Math.round(percentage * 100.0 * 100.0) / 100.0)
                            .build();
                })
                .filter(item -> item.getClicks() > 0)
                .collect(Collectors.toList());

        return DimensionStatResponse.builder()
                .category(dimensionType.name())
                .totalClicks(rangeTotalClicks)
                .data(data)
                .build();
    }

    @Transactional(readOnly = true)
    public DimensionStatResponse getDimensionStats(User user, DimensionType dimensionType) {
        List<DimensionStat> stats = dimensionStatRepository.findByUserAndTypeOrderByAllowedClicksDesc(user, dimensionType);
        return buildDimensionStatResponse(stats, dimensionType);
    }

    private DimensionStatResponse buildDimensionStatResponse(List<DimensionStat> stats, DimensionType dimensionType) {
        long totalClicks = stats.stream()
                .mapToLong(DimensionStat::getAllowedClicks)
                .sum();
        
        List<DimensionStatItemResponse> data = stats.stream()
                .map(stat -> {
                    double percentage = totalClicks > 0 
                            ? (stat.getAllowedClicks() * 100.0) / totalClicks 
                            : 0.0;
                    return DimensionStatItemResponse.builder()
                            .name(stat.getValue())
                            .clicks(stat.getAllowedClicks())
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .build();
                })
                .collect(Collectors.toList());
        
        return DimensionStatResponse.builder()
                .category(dimensionType.name())
                .totalClicks(totalClicks)
                .data(data)
                .build();
    }

    @Transactional(readOnly = true)
    public GeographyStatResponse getGeographyStats(User user, String shortCode, DimensionType dimensionType) {
        ShortUrl shortUrl = shortUrlService.getShortUrlByShortCode(user, shortCode);
        List<DimensionStat> stats = dimensionStatRepository.findByShortUrlAndTypeOrderByAllowedClicksDesc(shortUrl, dimensionType);
        return buildGeographyStatResponse(stats, dimensionType);
    }

    @Transactional(readOnly = true)
    public GeographyStatResponse getGeographyStats(User user, DimensionType dimensionType, String fromStr, String toStr, String timezoneStr) {
        if (fromStr == null && toStr == null) {
            return getGeographyStats(user, dimensionType);
        }

        TimeRange tr = parseTimeRange(Granularity.DAILY, fromStr, toStr, timezoneStr);
        
        // 1. Get total clicks in range from ClickStat
        List<ClickStat> clickStats = clickStatRepository.findByUserAndGranularityAndBucketStartBetween(
                user, tr.granularity, tr.fromUtc, tr.toUtc);
        long rangeTotalAllowedClicks = clickStats.stream().mapToLong(ClickStat::getAllowedClicks).sum();
        long rangeTotalBlockedClicks = clickStats.stream().mapToLong(ClickStat::getBlockedClicks).sum();

        // 2. Get lifetime distribution from DimensionStat
        List<DimensionStat> lifetimeStats = dimensionStatRepository.findByUserAndTypeOrderByAllowedClicksDesc(user, dimensionType);
        long lifetimeTotalAllowedClicks = lifetimeStats.stream().mapToLong(DimensionStat::getAllowedClicks).sum();
        long lifetimeTotalBlockedClicks = lifetimeStats.stream().mapToLong(DimensionStat::getBlockedClicks).sum();

        // 3. Estimate range stats based on lifetime distribution
        List<GeographyStatItemResponse> data = lifetimeStats.stream()
                .map(stat -> {
                    double allowedPercentage = lifetimeTotalAllowedClicks > 0 
                            ? (double) stat.getAllowedClicks() / lifetimeTotalAllowedClicks 
                            : 0.0;
                    double blockedPercentage = lifetimeTotalBlockedClicks > 0 
                            ? (double) stat.getBlockedClicks() / lifetimeTotalBlockedClicks 
                            : 0.0;
                            
                    long estimatedAllowedClicks = (long) (rangeTotalAllowedClicks * allowedPercentage);
                    long estimatedBlockedClicks = (long) (rangeTotalBlockedClicks * blockedPercentage);
                    long totalEstimatedClicks = estimatedAllowedClicks + estimatedBlockedClicks;
                    
                    double totalPercentage = (rangeTotalAllowedClicks + rangeTotalBlockedClicks) > 0
                            ? (double) totalEstimatedClicks / (rangeTotalAllowedClicks + rangeTotalBlockedClicks) * 100
                            : 0.0;

                    return GeographyStatItemResponse.builder()
                            .name(stat.getValue())
                            .clicks(estimatedAllowedClicks) // Using allowed clicks as main metric
                            .percentage(Math.round(totalPercentage * 100.0) / 100.0)
                            .allowedClicks(estimatedAllowedClicks)
                            .blockedClicks(estimatedBlockedClicks)
                            .build();
                })
                .filter(item -> (item.getAllowedClicks() + item.getBlockedClicks()) > 0)
                .collect(Collectors.toList());

        return GeographyStatResponse.builder()
                .category(dimensionType.name())
                .totalClicks(rangeTotalAllowedClicks + rangeTotalBlockedClicks)
                .totalAllowedClicks(rangeTotalAllowedClicks)
                .totalBlockedClicks(rangeTotalBlockedClicks)
                .data(data)
                .build();
    }

    @Transactional(readOnly = true)
    public GeographyStatResponse getGeographyStats(User user, DimensionType dimensionType) {
        List<DimensionStat> stats = dimensionStatRepository.findByUserAndTypeOrderByAllowedClicksDesc(user, dimensionType);
        return buildGeographyStatResponse(stats, dimensionType);
    }

    private GeographyStatResponse buildGeographyStatResponse(List<DimensionStat> stats, DimensionType dimensionType) {
        long totalAllowedClicks = stats.stream()
                .mapToLong(DimensionStat::getAllowedClicks)
                .sum();
        
        long totalBlockedClicks = stats.stream()
                .mapToLong(DimensionStat::getBlockedClicks)
                .sum();
        
        List<GeographyStatItemResponse> data = stats.stream()
                .map(stat -> {
                    double percentage = totalAllowedClicks > 0 
                            ? (stat.getAllowedClicks() * 100.0) / totalAllowedClicks 
                            : 0.0;
                    return GeographyStatItemResponse.builder()
                            .name(stat.getValue())
                            .clicks(stat.getAllowedClicks())
                            .percentage(Math.round(percentage * 100.0) / 100.0)
                            .allowedClicks(stat.getAllowedClicks())
                            .blockedClicks(stat.getBlockedClicks())
                            .build();
                })
                .collect(Collectors.toList());
        
        return GeographyStatResponse.builder()
                .category(dimensionType.name())
                .totalClicks(totalAllowedClicks + totalBlockedClicks)
                .totalAllowedClicks(totalAllowedClicks)
                .totalBlockedClicks(totalBlockedClicks)
                .data(data)
                .build();
    }

    @Transactional(readOnly = true)
    public TimeSeriesStatResponse getTimeSeriesStats(
            User user,
            String shortCode,
            Granularity granularity,
            String fromStr,
            String toStr,
            String timezoneStr
    ) {
        TimeRange tr = parseTimeRange(granularity, fromStr, toStr, timezoneStr);
        
        ShortUrl shortUrl = shortUrlService.getShortUrlByShortCode(user, shortCode);
        List<ClickStat> stats = clickStatRepository.findByShortUrlAndGranularityAndBucketStartBetween(
                shortUrl, tr.granularity, tr.fromUtc, tr.toUtc);
        
        return buildTimeSeriesStatResponse(stats, tr);
    }

    @Transactional(readOnly = true)
    public TimeSeriesStatResponse getTimeSeriesStats(
            User user,
            Granularity granularity,
            String fromStr,
            String toStr,
            String timezoneStr
    ) {
        TimeRange tr = parseTimeRange(granularity, fromStr, toStr, timezoneStr);
        
        List<ClickStat> stats = clickStatRepository.findByUserAndGranularityAndBucketStartBetween(
                user, tr.granularity, tr.fromUtc, tr.toUtc);
        
        return buildTimeSeriesStatResponse(stats, tr);
    }

    private TimeSeriesStatResponse buildTimeSeriesStatResponse(List<ClickStat> stats, TimeRange tr) {
        // Create map for quick lookup
        Map<Instant, ClickStat> statsMap = stats.stream()
                .collect(Collectors.toMap(ClickStat::getBucketStart, stat -> stat));
        
        // Generate all buckets and fill with data
        List<TimeSeriesStatItemResponse> data = new ArrayList<>();
        ZonedDateTime current = alignToBucketStart(tr.fromZoned, tr.granularity);
        long totalClicks = 0;
        long totalAllowedClicks = 0;
        long totalBlockedClicks = 0;
        
        while (current.isBefore(tr.toZoned) || current.equals(tr.toZoned)) {
            ZonedDateTime bucketEnd = addOneBucket(current, tr.granularity);
            
            // Convert to UTC to lookup in map
            Instant bucketStartUtc = current.withZoneSameInstant(ZoneOffset.UTC).toInstant();
            ClickStat stat = statsMap.get(bucketStartUtc);
            
            long clicks = stat != null ? stat.getTotalClicks() : 0L;
            long allowedClicks = stat != null ? stat.getAllowedClicks() : 0L;
            long blockedClicks = stat != null ? stat.getBlockedClicks() : 0L;
            
            totalClicks += clicks;
            totalAllowedClicks += allowedClicks;
            totalBlockedClicks += blockedClicks;
            
            data.add(TimeSeriesStatItemResponse.builder()
                    .bucketStart(current.format(ISO_FORMATTER))
                    .bucketEnd(bucketEnd.format(ISO_FORMATTER))
                    .clicks(clicks)
                    .allowedClicks(allowedClicks)
                    .blockedClicks(blockedClicks)
                    .build());
            
            current = bucketEnd;
        }
        
        return TimeSeriesStatResponse.builder()
                .granularity(tr.granularity.name())
                .timezone(tr.timezone.getId())
                .from(tr.fromZoned.format(ISO_FORMATTER))
                .to(tr.toZoned.format(ISO_FORMATTER))
                .totalClicks(totalClicks)
                .totalAllowedClicks(totalAllowedClicks)
                .totalBlockedClicks(totalBlockedClicks)
                .data(data)
                .build();
    }
    
    @Transactional(readOnly = true)
    public PeakTimeStatResponse getPeakTimeStats(
            User user,
            String shortCode,
            Granularity granularity,
            String fromStr,
            String toStr,
            String timezoneStr,
            Integer limit
    ) {
        TimeRange tr = parseTimeRange(granularity, fromStr, toStr, timezoneStr);
        int effectiveLimit = limit != null && limit > 0 ? limit : 10;
        
        ShortUrl shortUrl = shortUrlService.getShortUrlByShortCode(user, shortCode);
        List<ClickStat> stats = clickStatRepository.findTopByShortUrlAndGranularityAndBucketStartBetween(
                shortUrl, tr.granularity, tr.fromUtc, tr.toUtc, PageRequest.of(0, effectiveLimit));
        
        List<ClickStat> allStats = clickStatRepository.findByShortUrlAndGranularityAndBucketStartBetween(
                shortUrl, tr.granularity, tr.fromUtc, tr.toUtc);
        
        return buildPeakTimeStatResponse(stats, allStats, tr);
    }

    @Transactional(readOnly = true)
    public PeakTimeStatResponse getPeakTimeStats(
            User user,
            Granularity granularity,
            String fromStr,
            String toStr,
            String timezoneStr,
            Integer limit
    ) {
        TimeRange tr = parseTimeRange(granularity, fromStr, toStr, timezoneStr);
        int effectiveLimit = limit != null && limit > 0 ? limit : 10;
        
        List<ClickStat> stats = clickStatRepository.findTopByUserAndGranularityAndBucketStartBetween(
                user, tr.granularity, tr.fromUtc, tr.toUtc, PageRequest.of(0, effectiveLimit));
        
        List<ClickStat> allStats = clickStatRepository.findByUserAndGranularityAndBucketStartBetween(
                user, tr.granularity, tr.fromUtc, tr.toUtc);
        
        return buildPeakTimeStatResponse(stats, allStats, tr);
    }

    private PeakTimeStatResponse buildPeakTimeStatResponse(List<ClickStat> stats, List<ClickStat> allStats, TimeRange tr) {
        // Convert to response with timezone conversion
        List<TimeSeriesStatItemResponse> data = stats.stream()
                .map(stat -> {
                    ZonedDateTime bucketStart = stat.getBucketStart().atZone(ZoneOffset.UTC).withZoneSameInstant(tr.timezone);
                    ZonedDateTime bucketEnd = stat.getBucketEnd().atZone(ZoneOffset.UTC).withZoneSameInstant(tr.timezone);
                    
                    return TimeSeriesStatItemResponse.builder()
                            .bucketStart(bucketStart.format(ISO_FORMATTER))
                            .bucketEnd(bucketEnd.format(ISO_FORMATTER))
                            .clicks(stat.getTotalClicks())
                            .allowedClicks(stat.getAllowedClicks())
                            .blockedClicks(stat.getBlockedClicks())
                            .build();
                })
                .collect(Collectors.toList());
        
        return PeakTimeStatResponse.builder()
                .granularity(tr.granularity.name())
                .timezone(tr.timezone.getId())
                .from(tr.fromZoned.format(ISO_FORMATTER))
                .to(tr.toZoned.format(ISO_FORMATTER))
                .totalBuckets(allStats.size())
                .returnedBuckets(data.size())
                .data(data)
                .build();
    }

    private record TimeRange(Granularity granularity, ZoneId timezone, ZonedDateTime fromZoned, ZonedDateTime toZoned, Instant fromUtc, Instant toUtc) {}

    private TimeRange parseTimeRange(Granularity granularity, String fromStr, String toStr, String timezoneStr) {
        ZoneId timezone = timezoneStr != null ? ZoneId.of(timezoneStr) : ZoneOffset.UTC;
        Granularity effectiveGranularity = granularity != null ? granularity : Granularity.HOURLY;
        
        ZonedDateTime toZoned;
        ZonedDateTime fromZoned;
        
        if (toStr != null) {
            toZoned = parseDateTime(toStr, timezone);
        } else {
            toZoned = ZonedDateTime.now(timezone);
        }
        
        if (fromStr != null) {
            fromZoned = parseDateTime(fromStr, timezone);
        } else {
            int defaultBuckets = getDefaultBuckets(effectiveGranularity);
            fromZoned = subtractBuckets(toZoned, effectiveGranularity, defaultBuckets);
        }
        
        Instant fromUtc = fromZoned.withZoneSameInstant(ZoneOffset.UTC).toInstant();
        Instant toUtc = toZoned.withZoneSameInstant(ZoneOffset.UTC).toInstant();
        
        return new TimeRange(effectiveGranularity, timezone, fromZoned, toZoned, fromUtc, toUtc);
    }
    
    private int getDefaultBuckets(Granularity granularity) {
        return switch (granularity) {
            case HOURLY -> 24;
            case DAILY -> 30;
            case WEEKLY -> 12;
            case MONTHLY -> 24;
            case YEARLY -> 10;
        };
    }
    
    private ZonedDateTime subtractBuckets(ZonedDateTime dateTime, Granularity granularity, int buckets) {
        return switch (granularity) {
            case HOURLY -> dateTime.minusHours(buckets);
            case DAILY -> dateTime.minusDays(buckets);
            case WEEKLY -> dateTime.minusWeeks(buckets);
            case MONTHLY -> dateTime.minusMonths(buckets);
            case YEARLY -> dateTime.minusYears(buckets);
        };
    }
    
    private ZonedDateTime addOneBucket(ZonedDateTime dateTime, Granularity granularity) {
        return switch (granularity) {
            case HOURLY -> dateTime.plusHours(1);
            case DAILY -> dateTime.plusDays(1);
            case WEEKLY -> dateTime.plusWeeks(1);
            case MONTHLY -> dateTime.plusMonths(1);
            case YEARLY -> dateTime.plusYears(1);
        };
    }
    
    private ZonedDateTime alignToBucketStart(ZonedDateTime dateTime, Granularity granularity) {
        return switch (granularity) {
            case HOURLY -> dateTime.truncatedTo(ChronoUnit.HOURS);
            case DAILY -> dateTime.truncatedTo(ChronoUnit.DAYS);
            case WEEKLY -> dateTime.with(java.time.temporal.TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
                    .truncatedTo(ChronoUnit.DAYS);
            case MONTHLY -> dateTime.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth())
                    .truncatedTo(ChronoUnit.DAYS);
            case YEARLY -> dateTime.with(java.time.temporal.TemporalAdjusters.firstDayOfYear())
                    .truncatedTo(ChronoUnit.DAYS);
        };
    }
    
    /**
     * Parse datetime string supporting both formats:
     * - Full ISO 8601: "2025-12-11T00:00:00Z" or "2025-12-11T00:00:00+07:00"
     * - Date only: "2025-12-11" (will be parsed as 00:00:00 in the given timezone)
     */
    private ZonedDateTime parseDateTime(String dateTimeStr, ZoneId timezone) {
        if (dateTimeStr == null || dateTimeStr.isBlank()) {
            throw new IllegalArgumentException("Date time string cannot be null or blank");
        }
        
        try {
            // Try parsing as full ISO 8601 first
            if (dateTimeStr.contains("T")) {
                return ZonedDateTime.parse(dateTimeStr, ISO_FORMATTER).withZoneSameInstant(timezone);
            } else {
                // Parse as date only (YYYY-MM-DD) and set to start of day in the given timezone
                LocalDate date = LocalDate.parse(dateTimeStr);
                return date.atStartOfDay(timezone);
            }
        } catch (Exception e) {
            throw new IllegalArgumentException(
                "Invalid date time format. Expected ISO 8601 (e.g., '2025-12-11T00:00:00Z') or date only (e.g., '2025-12-11')", e);
        }
    }
}
