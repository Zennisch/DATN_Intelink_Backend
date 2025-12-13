package intelink.modules.redirect.services;

import intelink.models.ShortUrl;
import intelink.models.enums.ClickStatus;
import intelink.models.enums.Granularity;
import intelink.modules.redirect.repositories.ClickStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickStatService {

    private final ClickStatRepository clickStatRepository;

    @Transactional
    public void recordClickStats(ShortUrl shortUrl, ClickStatus status) {
        Instant now = Instant.now();
        int isAllowed = (status == ClickStatus.ALLOWED) ? 1 : 0;
        for (Granularity granularity : Granularity.values()) {
            Instant bucketStart = getBucketStat(now, granularity, ZoneOffset.UTC);
            Instant bucketEnd = getBucketEnd(bucketStart, granularity, ZoneOffset.UTC);
            try {
                // Use atomic MERGE operation to avoid deadlock
                clickStatRepository.upsertAndIncrement(
                    shortUrl.getId(),
                    granularity.name(),
                    bucketStart,
                    bucketEnd,
                    isAllowed
                );
            } catch (Exception e) {
                log.error("[ClickStatService.recordClickStats] Error upserting click stat for ShortUrl ID {}: {}", shortUrl.getId(), e.getMessage());
            }
        }
    }

    private Instant getBucketStat(Instant instant, Granularity granularity, ZoneId zoneId) {
        ZonedDateTime zdt = instant.atZone(zoneId);
        ZonedDateTime start;
        switch (granularity) {
            case HOURLY -> start = zdt.truncatedTo(ChronoUnit.HOURS);
            case DAILY -> start = zdt.truncatedTo(ChronoUnit.DAYS);
            case WEEKLY -> start = zdt.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).truncatedTo(ChronoUnit.DAYS);
            case MONTHLY -> start = zdt.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
            case YEARLY -> start = zdt.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
            default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        }
        return start.toInstant();
    }

    private Instant getBucketEnd(Instant instant, Granularity granularity, ZoneId zoneId) {
        ZonedDateTime start = getBucketStat(instant, granularity, zoneId).atZone(zoneId);
        ZonedDateTime end;
        switch (granularity) {
            case HOURLY -> end = start.plusHours(1);
            case DAILY -> end = start.plusDays(1);
            case WEEKLY -> end = start.plusWeeks(1);
            case MONTHLY -> end = start.plusMonths(1);
            case YEARLY -> end = start.plusYears(1);
            default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        }
        return end.toInstant();
    }

}
