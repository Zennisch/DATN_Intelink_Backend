package intelink.utils.seeder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import org.springframework.stereotype.Component;

import intelink.models.enums.Granularity;

@Component
public class DataSeedingUtils {

    public final List<String> countries = Arrays.asList("US", "UK", "DE", "FR", "JP", "CN", "IN", "BR", "CA", "AU", "VN", "TH", "SG", "MY", "PH");
    public final List<String> cities = Arrays.asList("New York", "London", "Berlin", "Paris", "Tokyo", "Shanghai", "Mumbai", "São Paulo", "Toronto", "Sydney", "Ho Chi Minh City", "Bangkok", "Singapore", "Kuala Lumpur", "Manila");
    public final List<String> browsers = Arrays.asList("Chrome", "Firefox", "Safari", "Edge", "Opera", "IE");
    public final List<String> operatingSystems = Arrays.asList("Windows", "macOS", "Linux", "Android", "iOS", "ChromeOS");
    public final List<String> deviceTypes = Arrays.asList("Desktop", "Mobile", "Tablet", "Smart TV", "Game Console");
    public final List<String> userAgents = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Android 14; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0"
    );
    public final List<String> domains = Arrays.asList("google.com", "github.com", "stackoverflow.com", "youtube.com", "facebook.com", "twitter.com", "linkedin.com", "reddit.com", "medium.com", "dev.to");
    private final Random random = new Random();

    public Instant getRandomInstantBetween(int startYear, int endYear) {
        LocalDateTime start = LocalDateTime.of(startYear, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(endYear, 12, 31, 23, 59);
        long startEpoch = start.toEpochSecond(ZoneOffset.UTC);
        long endEpoch = end.toEpochSecond(ZoneOffset.UTC);
        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return Instant.ofEpochSecond(randomEpoch);
    }

    public String generateRandomIp() {
        return random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
    }

    public Instant getBucketStart(Instant timestamp, Granularity granularity) {
        LocalDateTime ldt = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
        LocalDateTime start;
        switch (granularity) {
            case HOURLY -> start = ldt.truncatedTo(ChronoUnit.HOURS);
            case DAILY -> start = ldt.truncatedTo(ChronoUnit.DAYS);
            case WEEKLY -> start = ldt.with(java.time.temporal.TemporalAdjusters.previousOrSame(java.time.DayOfWeek.MONDAY)).truncatedTo(ChronoUnit.DAYS);
            case MONTHLY -> start = ldt.with(java.time.temporal.TemporalAdjusters.firstDayOfMonth()).truncatedTo(ChronoUnit.DAYS);
            case YEARLY -> start = ldt.with(java.time.temporal.TemporalAdjusters.firstDayOfYear()).truncatedTo(ChronoUnit.DAYS);
            default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        }
        return start.toInstant(ZoneOffset.UTC);
    }
    
    public Instant getBucketEnd(Instant bucketStart, Granularity granularity) {
        LocalDateTime start = LocalDateTime.ofInstant(bucketStart, ZoneOffset.UTC);
        LocalDateTime end;
        switch (granularity) {
            case HOURLY -> end = start.plusHours(1);
            case DAILY -> end = start.plusDays(1);
            case WEEKLY -> end = start.plusWeeks(1);
            case MONTHLY -> end = start.plusMonths(1);
            case YEARLY -> end = start.plusYears(1);
            default -> throw new IllegalArgumentException("Unsupported granularity: " + granularity);
        }
        return end.toInstant(ZoneOffset.UTC);
    }

    public <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public Random getRandom() {
        return random;
    }
}
