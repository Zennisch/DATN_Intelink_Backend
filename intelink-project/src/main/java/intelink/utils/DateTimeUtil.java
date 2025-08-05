package intelink.utils;

import intelink.models.enums.Granularity;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

public class DateTimeUtil {

    public static Instant getBucketStart(Instant instant, Granularity granularity) {
        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        switch (granularity) {
            case HOURLY:
                return zdt.truncatedTo(ChronoUnit.HOURS).toInstant();
            case DAILY:
                return zdt.toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
            case MONTHLY:
                return zdt.withDayOfMonth(1).toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
            case YEARLY:
                return zdt.withDayOfYear(1).toLocalDate().atStartOfDay(zdt.getZone()).toInstant();
            default:
                throw new IllegalArgumentException("DateTimeUtil.getBucketStart: Unsupported granularity: " + granularity);
        }
    }

}
