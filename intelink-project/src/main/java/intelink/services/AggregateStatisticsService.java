// java
package intelink.services;

import intelink.dto.response.stat.*;
import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.repositories.ClickStatRepository;
import intelink.repositories.DimensionStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AggregateStatisticsService {

    private final DimensionStatRepository dimensionStatRepository;
    private final ClickStatRepository clickStatRepository;

    private List<String> parseShortCodes(String csv) {
        if (csv == null || csv.isBlank()) return null;
        return Arrays.stream(csv.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    private Instant parseToInstantStart(String s) {
        if (s == null) return null;
        if (s.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            LocalDate d = LocalDate.parse(s);
            return d.atStartOfDay(ZoneOffset.UTC).toInstant();
        }
        return Instant.parse(s);
    }

    private Instant parseToInstantEnd(String s) {
        if (s == null) return null;
        if (s.matches("^\\d{4}-\\d{2}-\\d{2}$")) {
            LocalDate d = LocalDate.parse(s);
            return d.atTime(LocalTime.MAX).atZone(ZoneOffset.UTC).toInstant();
        }
        return Instant.parse(s);
    }

    public AggregateByCountryResponse getByCountry(String shortCodesCsv, String from, String to, Integer limit) {
        List<String> shortCodes = parseShortCodes(shortCodesCsv);

        // parse date range (use start for from, end for to). If both null, keep old behavior.
        Instant fromInst = parseToInstantStart(from);
        Instant toInst = parseToInstantEnd(to);

        Map<String, Long> agg = new HashMap<>();

        if (fromInst == null && toInst == null) {
            // original behavior: lifetime totals stored in DimensionStat
            List<DimensionStat> stats;
            if (shortCodes == null) {
                stats = dimensionStatRepository.findByType(DimensionType.COUNTRY);
            } else {
                stats = dimensionStatRepository.findByTypeAndShortUrl_ShortCodeIn(DimensionType.COUNTRY, shortCodes);
            }

            for (DimensionStat s : stats) {
                String country = s.getValue() == null ? "UNKNOWN" : s.getValue();
                agg.put(country, agg.getOrDefault(country, 0L) + s.getTotalClicks());
            }
        } else {
            // ensure both bounds are present
            Instant now = Instant.now();
            toInst = (toInst == null) ? now : toInst;
            fromInst = (fromInst == null) ? toInst.minus(29, ChronoUnit.DAYS) : fromInst;

            // fetch click stats in range (if your repository method names differ adjust accordingly)
            List<ClickStat> clickStats;
            if (shortCodes == null) {
                clickStats = clickStatRepository.findByBucketBetween(fromInst, toInst);
            } else {
                clickStats = clickStatRepository.findByShortUrl_ShortCodeInAndBucketBetween(shortCodes, fromInst, toInst);
            }

            // aggregate clicks by shortCode
            Map<String, Long> clicksByShortCode = new HashMap<>();
            for (ClickStat cs : clickStats) {
                if (cs.getShortUrl() == null || cs.getShortUrl().getShortCode() == null) continue;
                String sc = cs.getShortUrl().getShortCode();
                clicksByShortCode.merge(sc, cs.getTotalClicks(), Long::sum);
            }

            if (!clicksByShortCode.isEmpty()) {
                // map shortCode -> country using DimensionStat entries
                List<String> shortCodesInClicks = new ArrayList<>(clicksByShortCode.keySet());
                List<DimensionStat> countryStats = dimensionStatRepository.findByTypeAndShortUrl_ShortCodeIn(DimensionType.COUNTRY, shortCodesInClicks);

                Map<String, String> shortCodeToCountry = new HashMap<>();
                for (DimensionStat ds : countryStats) {
                    if (ds.getShortUrl() == null || ds.getShortUrl().getShortCode() == null) continue;
                    shortCodeToCountry.put(ds.getShortUrl().getShortCode(), ds.getValue() == null ? "UNKNOWN" : ds.getValue());
                }

                // aggregate per country
                for (Map.Entry<String, Long> e : clicksByShortCode.entrySet()) {
                    String sc = e.getKey();
                    Long clicks = e.getValue();
                    String country = shortCodeToCountry.getOrDefault(sc, "UNKNOWN");
                    agg.merge(country, clicks, Long::sum);
                }
            }
        }

        int effectiveLimit = limit != null ? limit : 10;
        List<CountryStat> list = agg.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(effectiveLimit)
                .map(e -> new CountryStat(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new AggregateByCountryResponse(effectiveLimit,from,to, list);
    }

    public TimeSeriesAggregateResponse getTimeSeries(String shortCodesCsv, String fromStr, String toStr, String granularityStr) {
        List<String> shortCodes = parseShortCodes(shortCodesCsv);
        Granularity granularity = granularityStr != null ? Granularity.fromString(granularityStr) : Granularity.DAILY;

        Instant from = parseToInstantStart(fromStr);
        Instant to = parseToInstantStart(toStr);

        if (from == null || to == null) {
            Instant now = Instant.now();
            to = (to == null) ? now : to;
            from = (from == null) ? to.minus(29, ChronoUnit.DAYS) : from;
        }

        switch (granularity) {
            case HOURLY -> {
                from = from.truncatedTo(ChronoUnit.HOURS);
                to = to.truncatedTo(ChronoUnit.HOURS);
            }
            case DAILY -> {
                from = LocalDateTime.ofInstant(from, ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
                to = LocalDateTime.ofInstant(to, ZoneOffset.UTC).toLocalDate().atStartOfDay(ZoneOffset.UTC).toInstant();
            }
            case MONTHLY -> {
                ZonedDateTime zf = from.atZone(ZoneOffset.UTC).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                ZonedDateTime zt = to.atZone(ZoneOffset.UTC).withDayOfMonth(1).truncatedTo(ChronoUnit.DAYS);
                from = zf.toInstant();
                to = zt.toInstant();
            }
            case YEARLY -> {
                ZonedDateTime zf = from.atZone(ZoneOffset.UTC).withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
                ZonedDateTime zt = to.atZone(ZoneOffset.UTC).withDayOfYear(1).truncatedTo(ChronoUnit.DAYS);
                from = zf.toInstant();
                to = zt.toInstant();
            }
        }

        List<ClickStat> stats;
        if (shortCodes == null) {
            stats = clickStatRepository.findByGranularityAndBucketBetween(granularity, from, to);
        } else {
            stats = clickStatRepository.findByShortUrl_ShortCodeInAndGranularityAndBucketBetween(shortCodes, granularity, from, to);
        }

        Map<Instant, Long> bucketClicks = stats.stream()
                .collect(Collectors.toMap(ClickStat::getBucket, ClickStat::getTotalClicks, Long::sum));

        List<TimeSeriesPoint> points = new ArrayList<>();
        Instant cursor = from;
        DateTimeFormatter dateFmt = DateTimeFormatter.ISO_LOCAL_DATE;
        long totalViews = 0L;

        while (!cursor.isAfter(to)) {
            long views = bucketClicks.getOrDefault(cursor, 0L);
            totalViews += views;
            String label;
            switch (granularity) {
                case HOURLY -> label = cursor.toString();
                case DAILY -> label = LocalDate.ofInstant(cursor, ZoneOffset.UTC).format(dateFmt);
                case MONTHLY -> {
                    ZonedDateTime z = cursor.atZone(ZoneOffset.UTC);
                    label = String.format("%04d-%02d", z.getYear(), z.getMonthValue());
                }
                case YEARLY -> label = String.valueOf(cursor.atZone(ZoneOffset.UTC).getYear());
                default -> label = cursor.toString();
            }
            points.add(new TimeSeriesPoint(label, views));

            switch (granularity) {
                case HOURLY -> cursor = cursor.plus(1, ChronoUnit.HOURS);
                case DAILY -> cursor = cursor.plus(1, ChronoUnit.DAYS);
                case MONTHLY -> cursor = cursor.atZone(ZoneOffset.UTC).plusMonths(1).toInstant();
                case YEARLY -> cursor = cursor.atZone(ZoneOffset.UTC).plusYears(1).toInstant();
            }
        }

        return new TimeSeriesAggregateResponse(
                granularity.name(),
                from.toString(),
                to.toString(),
                totalViews,
                points
        );
    }

    public AggregateByDimensionResponse getByDimension(String shortCodesCsv, String from, String to, Integer limit, DimensionType type) {
        List<String> shortCodes = parseShortCodes(shortCodesCsv);

        // parse date range (use start for from, end for to). If both null, use lifetime DimensionStat
        Instant fromInst = parseToInstantStart(from);
        Instant toInst = parseToInstantEnd(to);

        Map<String, Long> agg = new HashMap<>();

        if (fromInst == null && toInst == null) {
            // lifetime totals stored in DimensionStat for the requested type
            List<DimensionStat> stats;
            if (shortCodes == null) {
                stats = dimensionStatRepository.findByType(type);
            } else {
                stats = dimensionStatRepository.findByTypeAndShortUrl_ShortCodeIn(type, shortCodes);
            }

            for (DimensionStat s : stats) {
                String val = s.getValue() == null ? "UNKNOWN" : s.getValue();
                agg.put(val, agg.getOrDefault(val, 0L) + s.getTotalClicks());
            }
        } else {
            // ensure both bounds present
            Instant now = Instant.now();
            toInst = (toInst == null) ? now : toInst;
            fromInst = (fromInst == null) ? toInst.minus(29, ChronoUnit.DAYS) : fromInst;

            List<ClickStat> clickStats;
            if (shortCodes == null) {
                clickStats = clickStatRepository.findByBucketBetween(fromInst, toInst);
            } else {
                clickStats = clickStatRepository.findByShortUrl_ShortCodeInAndBucketBetween(shortCodes, fromInst, toInst);
            }

            Map<String, Long> clicksByShortCode = new HashMap<>();
            for (ClickStat cs : clickStats) {
                if (cs.getShortUrl() == null || cs.getShortUrl().getShortCode() == null) continue;
                String sc = cs.getShortUrl().getShortCode();
                clicksByShortCode.merge(sc, cs.getTotalClicks(), Long::sum);
            }

            if (!clicksByShortCode.isEmpty()) {
                List<String> shortCodesInClicks = new ArrayList<>(clicksByShortCode.keySet());
                List<DimensionStat> dimStats = dimensionStatRepository.findByTypeAndShortUrl_ShortCodeIn(type, shortCodesInClicks);

                Map<String, String> shortCodeToValue = new HashMap<>();
                for (DimensionStat ds : dimStats) {
                    if (ds.getShortUrl() == null || ds.getShortUrl().getShortCode() == null) continue;
                    shortCodeToValue.put(ds.getShortUrl().getShortCode(), ds.getValue() == null ? "UNKNOWN" : ds.getValue());
                }

                for (Map.Entry<String, Long> e : clicksByShortCode.entrySet()) {
                    String sc = e.getKey();
                    Long clicks = e.getValue();
                    String val = shortCodeToValue.getOrDefault(sc, "UNKNOWN");
                    agg.merge(val, clicks, Long::sum);
                }
            }
        }

        int effectiveLimit = limit != null ? limit : 10;
        List<DimensionStatDto> list = agg.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue(Comparator.reverseOrder()))
                .limit(effectiveLimit)
                .map(e -> new DimensionStatDto(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        return new AggregateByDimensionResponse(type.name(), effectiveLimit, from, to, list);
    }
}