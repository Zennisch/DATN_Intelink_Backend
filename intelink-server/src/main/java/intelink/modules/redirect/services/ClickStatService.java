package intelink.modules.redirect.services;

import intelink.models.ClickLog;
import intelink.models.ClickStat;
import intelink.models.ShortUrl;
import intelink.models.enums.ClickStatus;
import intelink.models.enums.Granularity;
import intelink.modules.redirect.repositories.ClickStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
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
        for (Granularity granularity : Granularity.values()) {
            Instant bucketStart = getBucketStat(now, granularity);
            Instant bucketEnd = getBucketEnd(bucketStart, granularity);
            ClickStat clickStat = clickStatRepository
                    .findByShortUrlAndGranularityAndBucketStart(shortUrl, granularity, bucketStart)
                    .orElseGet(() -> {
                        ClickStat c = ClickStat.builder()
                                .shortUrl(shortUrl)
                                .granularity(granularity)
                                .bucketStart(bucketStart)
                                .bucketEnd(bucketEnd)
                                .build();
                        return clickStatRepository.save(c);
                    });

            if (status == ClickStatus.ALLOWED) {
                clickStatRepository.increaseAllowedCounters(clickStat.getId());
            } else if (status == ClickStatus.BLOCKED) {
                clickStatRepository.increaseBlockedCounters(clickStat.getId());
            }
        }
    }

    private Instant getBucketStat(Instant instant, Granularity granularity) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime zdt = instant.atZone(zone);
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

    private Instant getBucketEnd(Instant instant, Granularity granularity) {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime start = getBucketStat(instant, granularity).atZone(zone);
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
