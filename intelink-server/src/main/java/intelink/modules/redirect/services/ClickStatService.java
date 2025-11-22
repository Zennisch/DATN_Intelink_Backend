package intelink.modules.redirect.services;

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
                        ClickStat newStat = new ClickStat();
                        newStat.setShortUrl(shortUrl);
                        newStat.setGranularity(granularity);
                        newStat.setBucketStart(bucketStart);
                        newStat.setBucketEnd(bucketEnd);
                        return newStat;
                    });

            if (status == ClickStatus.ALLOWED) {
                clickStatRepository.increaseAllowedCounters(clickStat.getId());
            } else if (status == ClickStatus.BLOCKED) {
                clickStatRepository.increaseBlockedCounters(clickStat.getId());
            }
        }
    }

    private Instant getBucketStat(Instant instant, Granularity granularity) {
        switch (granularity) {
            case HOURLY -> instant.truncatedTo(ChronoUnit.HOURS);
            case DAILY -> instant.truncatedTo(ChronoUnit.DAYS);
            case WEEKLY ->
                    instant.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).truncatedTo(ChronoUnit.DAYS);
            case MONTHLY -> instant.with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
            case YEARLY -> instant.with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
            default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        }
        return instant;
    }

    private Instant getBucketEnd(Instant instant, Granularity granularity) {
        switch (granularity) {
            case HOURLY -> instant.plus(1, ChronoUnit.HOURS).truncatedTo(ChronoUnit.HOURS);
            case DAILY -> instant.plus(1, ChronoUnit.DAYS).truncatedTo(ChronoUnit.DAYS);
            case WEEKLY ->
                    instant.plus(1, ChronoUnit.WEEKS).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).truncatedTo(ChronoUnit.DAYS);
            case MONTHLY ->
                    instant.plus(1, ChronoUnit.MONTHS).with(TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
            case YEARLY ->
                    instant.plus(1, ChronoUnit.YEARS).with(TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
            default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        }
        return instant;
    }

}
