package intelink.utils.seeder;

import intelink.models.enums.*;
import intelink.utils.DateTimeUtil;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

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

    public Instant getRandomInstantAfter(Instant after) {
        return after.plus(ThreadLocalRandom.current().nextLong(1, 365), ChronoUnit.DAYS);
    }

    public String generateRandomShortCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    public String generateRandomIp() {
        return random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
    }

    public String generateRandomSubnet() {
        return generateRandomIp() + "/24";
    }

    public Instant getBucketStart(Instant timestamp, Granularity granularity) {
        return DateTimeUtil.getBucketStart(timestamp, granularity);
    }

    public <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    public UserProvider getRandomOAuthProvider() {
        UserProvider[] providers = {UserProvider.GOOGLE, UserProvider.GITHUB};
        return providers[random.nextInt(providers.length)];
    }

    public VerificationTokenType getRandomTokenType() {
        VerificationTokenType[] types = VerificationTokenType.values();
        return types[random.nextInt(types.length)];
    }

    public ShortUrlAnalysisStatus getRandomAnalysisStatus() {
        return random.nextDouble() < 0.7 ? ShortUrlAnalysisStatus.SAFE :
                random.nextDouble() < 0.9 ? ShortUrlAnalysisStatus.SUSPICIOUS : ShortUrlAnalysisStatus.MALICIOUS;
    }

    public ShortUrlAnalysisThreatType getRandomThreatType() {
        ShortUrlAnalysisThreatType[] threats = {
                ShortUrlAnalysisThreatType.MALWARE,
                ShortUrlAnalysisThreatType.PHISHING,
                ShortUrlAnalysisThreatType.SPAM,
                ShortUrlAnalysisThreatType.SCAM,
                ShortUrlAnalysisThreatType.OTHER
        };
        return threats[random.nextInt(threats.length)];
    }

    public ShortUrlAnalysisPlatformType getRandomPlatformType() {
        ShortUrlAnalysisPlatformType[] platforms = ShortUrlAnalysisPlatformType.values();
        return platforms[random.nextInt(platforms.length)];
    }

    public CustomDomainStatus getRandomDomainStatus() {
        CustomDomainStatus[] statuses = CustomDomainStatus.values();
        return statuses[random.nextInt(statuses.length)];
    }

    public CustomDomainVerificationMethod getRandomVerificationMethod() {
        CustomDomainVerificationMethod[] methods = CustomDomainVerificationMethod.values();
        return methods[random.nextInt(methods.length)];
    }

    public Random getRandom() {
        return random;
    }
}
