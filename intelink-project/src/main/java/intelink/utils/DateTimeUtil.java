package intelink.utils;

import intelink.models.enums.Granularity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {

    public static Instant getBucketStart(Instant instant, Granularity granularity) {
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        return switch (granularity) {
            case HOURLY -> zdt.truncatedTo(ChronoUnit.HOURS).toInstant();
            case DAILY -> zdt.toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
            case MONTHLY -> zdt.withDayOfMonth(1).toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
            case YEARLY -> zdt.withDayOfYear(1).toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
        };
    }

}
