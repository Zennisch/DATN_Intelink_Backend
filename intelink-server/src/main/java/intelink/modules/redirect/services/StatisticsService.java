package intelink.modules.redirect.services;

import intelink.dto.statistics.*;
import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
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
    private final ShortUrlService shortUrlService;
    
    private static final DateTimeFormatter ISO_FORMATTER = DateTimeFormatter.ISO_OFFSET_DATE_TIME;

    @Transactional(readOnly = true)
    public DimensionStatResponse getDimensionStats(User user, String shortCode, DimensionType dimensionType) {
        ShortUrl shortUrl = shortUrlService.getShortUrlByShortCode(user, shortCode);
        
        List<DimensionStat> stats = dimensionStatRepository.findByShortUrlAndTypeOrderByAllowedClicksDesc(shortUrl, dimensionType);
        
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
        // Parse timezone, default to UTC
        ZoneId timezone = timezoneStr != null ? ZoneId.of(timezoneStr) : ZoneOffset.UTC;
        
        // Parse granularity, default to HOURLY
        Granularity effectiveGranularity = granularity != null ? granularity : Granularity.HOURLY;
        
        // Parse from/to or use defaults
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
            // Calculate default from based on granularity
            int defaultBuckets = getDefaultBuckets(effectiveGranularity);
            fromZoned = subtractBuckets(toZoned, effectiveGranularity, defaultBuckets);
        }
        
        // Convert to UTC for database query
        Instant fromUtc = fromZoned.withZoneSameInstant(ZoneOffset.UTC).toInstant();
        Instant toUtc = toZoned.withZoneSameInstant(ZoneOffset.UTC).toInstant();
        
        // Query database
        ShortUrl shortUrl = shortUrlService.getShortUrlByShortCode(user, shortCode);
        List<ClickStat> stats = clickStatRepository.findByShortUrlAndGranularityAndBucketStartBetween(
                shortUrl, effectiveGranularity, fromUtc, toUtc);
        
        // Create map for quick lookup
        Map<Instant, ClickStat> statsMap = stats.stream()
                .collect(Collectors.toMap(ClickStat::getBucketStart, stat -> stat));
        
        // Generate all buckets and fill with data
        List<TimeSeriesStatItemResponse> data = new ArrayList<>();
        ZonedDateTime current = alignToBucketStart(fromZoned, effectiveGranularity);
        long totalClicks = 0;
        long totalAllowedClicks = 0;
        long totalBlockedClicks = 0;
        
        while (current.isBefore(toZoned) || current.equals(toZoned)) {
            ZonedDateTime bucketEnd = addOneBucket(current, effectiveGranularity);
            
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
                .granularity(effectiveGranularity.name())
                .timezone(timezone.getId())
                .from(fromZoned.format(ISO_FORMATTER))
                .to(toZoned.format(ISO_FORMATTER))
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
        // Parse timezone, default to UTC
        ZoneId timezone = timezoneStr != null ? ZoneId.of(timezoneStr) : ZoneOffset.UTC;
        
        // Parse granularity, default to HOURLY
        Granularity effectiveGranularity = granularity != null ? granularity : Granularity.HOURLY;
        
        // Parse limit, default to 10
        int effectiveLimit = limit != null && limit > 0 ? limit : 10;
        
        // Parse from/to or use defaults
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
            // Calculate default from based on granularity
            int defaultBuckets = getDefaultBuckets(effectiveGranularity);
            fromZoned = subtractBuckets(toZoned, effectiveGranularity, defaultBuckets);
        }
        
        // Convert to UTC for database query
        Instant fromUtc = fromZoned.withZoneSameInstant(ZoneOffset.UTC).toInstant();
        Instant toUtc = toZoned.withZoneSameInstant(ZoneOffset.UTC).toInstant();
        
        // Query database with limit
        ShortUrl shortUrl = shortUrlService.getShortUrlByShortCode(user, shortCode);
        List<ClickStat> stats = clickStatRepository.findTopByShortUrlAndGranularityAndBucketStartBetween(
                shortUrl, effectiveGranularity, fromUtc, toUtc, PageRequest.of(0, effectiveLimit));
        
        // Convert to response with timezone conversion
        List<TimeSeriesStatItemResponse> data = stats.stream()
                .map(stat -> {
                    ZonedDateTime bucketStart = stat.getBucketStart().atZone(ZoneOffset.UTC).withZoneSameInstant(timezone);
                    ZonedDateTime bucketEnd = stat.getBucketEnd().atZone(ZoneOffset.UTC).withZoneSameInstant(timezone);
                    
                    return TimeSeriesStatItemResponse.builder()
                            .bucketStart(bucketStart.format(ISO_FORMATTER))
                            .bucketEnd(bucketEnd.format(ISO_FORMATTER))
                            .clicks(stat.getTotalClicks())
                            .allowedClicks(stat.getAllowedClicks())
                            .blockedClicks(stat.getBlockedClicks())
                            .build();
                })
                .collect(Collectors.toList());
        
        // Count total buckets in the range
        List<ClickStat> allStats = clickStatRepository.findByShortUrlAndGranularityAndBucketStartBetween(
                shortUrl, effectiveGranularity, fromUtc, toUtc);
        
        return PeakTimeStatResponse.builder()
                .granularity(effectiveGranularity.name())
                .timezone(timezone.getId())
                .from(fromZoned.format(ISO_FORMATTER))
                .to(toZoned.format(ISO_FORMATTER))
                .totalBuckets(allStats.size())
                .returnedBuckets(data.size())
                .data(data)
                .build();
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
