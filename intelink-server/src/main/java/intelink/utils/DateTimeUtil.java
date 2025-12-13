package intelink.utils;

import intelink.models.enums.Granularity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {

    public static Instant getBucketStart(Instant instant, Granularity granularity) {
        return getBucketStart(instant, granularity, ZoneId.of("UTC"));
    }

    public static Instant getBucketStart(Instant instant, Granularity granularity, ZoneId zoneId) {
        ZonedDateTime zdt = instant.atZone(zoneId);
        return switch (granularity) {
            case HOURLY -> zdt.truncatedTo(ChronoUnit.HOURS).toInstant();
            case DAILY -> zdt.toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
            case WEEKLY -> zdt.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY))
                    .toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
            case MONTHLY -> zdt.withDayOfMonth(1).toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
            case YEARLY -> zdt.withDayOfYear(1).toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
        };
    }

}
