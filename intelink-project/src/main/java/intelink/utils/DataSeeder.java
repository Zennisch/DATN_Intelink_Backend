package intelink.utils;

import intelink.models.*;
import intelink.models.enums.*;
import intelink.repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

//import org.springframework.stereotype.Component;
//@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final ShortUrlRepository shortUrlRepository;
    private final ClickLogRepository clickLogRepository;
    private final ClickStatRepository clickStatRepository;
    private final DimensionStatRepository dimensionStatRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random();
    private final List<String> countries = Arrays.asList("US", "UK", "DE", "FR", "JP", "CN", "IN", "BR", "CA", "AU", "VN", "TH", "SG", "MY", "PH");
    private final List<String> cities = Arrays.asList("New York", "London", "Berlin", "Paris", "Tokyo", "Shanghai", "Mumbai", "São Paulo", "Toronto", "Sydney", "Ho Chi Minh City", "Bangkok", "Singapore", "Kuala Lumpur", "Manila");
    private final List<String> browsers = Arrays.asList("Chrome", "Firefox", "Safari", "Edge", "Opera", "IE");
    private final List<String> operatingSystems = Arrays.asList("Windows", "macOS", "Linux", "Android", "iOS", "ChromeOS");
    private final List<String> deviceTypes = Arrays.asList("Desktop", "Mobile", "Tablet", "Smart TV", "Game Console");
    private final List<String> userAgents = Arrays.asList(
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (iPhone; CPU iPhone OS 17_1 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/17.1 Mobile/15E148 Safari/604.1",
            "Mozilla/5.0 (Android 14; Mobile; rv:120.0) Gecko/120.0 Firefox/120.0"
    );
    private final List<String> domains = Arrays.asList("google.com", "github.com", "stackoverflow.com", "youtube.com", "facebook.com", "twitter.com", "linkedin.com", "reddit.com", "medium.com", "dev.to");

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Data already exists, skipping seeding");
            return;
        }

        log.info("Starting data seeding...");

        List<User> users = createUsers(100);
        log.info("Created {} users", users.size());

        List<ShortUrl> shortUrls = createShortUrls(users, 500);
        log.info("Created {} short URLs", shortUrls.size());

        createVerificationTokens(users, 50);
        log.info("Created verification tokens");

        createOAuthAccounts(users, 30);
        log.info("Created OAuth accounts");

        createAnalysisResults(shortUrls, 200);
        log.info("Created analysis results");

        createClickLogsAndStats(shortUrls, 10000);
        log.info("Created click logs and stats");

        log.info("Data seeding completed successfully!");
    }

    private List<User> createUsers(int count) {
        List<User> users = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            User user = User.builder()
                    .username("user" + String.format("%03d", i))
                    .email("user" + i + "@example.com")
                    .passwordHash(passwordEncoder.encode("password123"))
                    .role(i <= 5 ? UserRole.ADMIN : UserRole.USER)
                    .totalClicks(ThreadLocalRandom.current().nextLong(0, 1000))
                    .totalShortUrls(ThreadLocalRandom.current().nextInt(0, 50))
                    .emailVerified(random.nextBoolean())
                    .authProvider(random.nextDouble() < 0.7 ? OAuthProvider.LOCAL : getRandomOAuthProvider())
                    .providerUserId(random.nextDouble() < 0.3 ? "provider_" + UUID.randomUUID().toString().substring(0, 8) : null)
                    .lastLoginAt(getRandomInstantBetween(2021, 2024))
                    .createdAt(getRandomInstantBetween(2021, 2023))
                    .updatedAt(Instant.now())
                    .build();

            users.add(user);
        }

        return userRepository.saveAll(users);
    }

    private List<ShortUrl> createShortUrls(List<User> users, int count) {
        List<ShortUrl> shortUrls = new ArrayList<>();

        for (int i = 1; i <= count; i++) {
            User randomUser = users.get(random.nextInt(users.size()));
            Instant createdAt = getRandomInstantBetween(2021, 2024);

            ShortUrl shortUrl = ShortUrl.builder()
                    .shortCode(generateRandomShortCode())
                    .originalUrl("https://" + getRandomElement(domains) + "/page/" + i)
                    .password(random.nextDouble() < 0.2 ? passwordEncoder.encode("secret123") : null)
                    .description(random.nextDouble() < 0.5 ? "Description for URL " + i : null)
                    .status(getRandomShortUrlStatus())
                    .maxUsage(random.nextDouble() < 0.3 ? ThreadLocalRandom.current().nextLong(10, 1000) : null)
                    .totalClicks(ThreadLocalRandom.current().nextLong(0, 500))
                    .expiresAt(createdAt.plus(ThreadLocalRandom.current().nextLong(30, 365), ChronoUnit.DAYS))
                    .createdAt(createdAt)
                    .updatedAt(getRandomInstantAfter(createdAt))
                    .user(randomUser)
                    .build();

            shortUrls.add(shortUrl);
        }

        return shortUrlRepository.saveAll(shortUrls);
    }

    private void createVerificationTokens(List<User> users, int count) {
        List<VerificationToken> tokens = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User randomUser = users.get(random.nextInt(users.size()));
            Instant createdAt = getRandomInstantBetween(2021, 2024);

            VerificationToken token = VerificationToken.builder()
                    .token(UUID.randomUUID().toString())
                    .type(getRandomTokenType())
                    .used(random.nextBoolean())
                    .expiresAt(createdAt.plus(24, ChronoUnit.HOURS))
                    .createdAt(createdAt)
                    .user(randomUser)
                    .build();

            tokens.add(token);
        }

        verificationTokenRepository.saveAll(tokens);
    }

    private void createOAuthAccounts(List<User> users, int count) {
        List<OAuthAccount> accounts = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            User randomUser = users.get(random.nextInt(users.size()));
            OAuthProvider provider = getRandomOAuthProvider();
            Instant createdAt = getRandomInstantBetween(2021, 2024);

            OAuthAccount account = OAuthAccount.builder()
                    .provider(provider)
                    .providerUserId("oauth_" + provider.name().toLowerCase() + "_" + i)
                    .providerUsername("oauth_user_" + i)
                    .providerEmail("oauth" + i + "@" + provider.name().toLowerCase() + ".com")
                    .accessToken("access_token_" + UUID.randomUUID())
                    .refreshToken(random.nextDouble() < 0.7 ? "refresh_token_" + UUID.randomUUID() : null)
                    .tokenExpiresAt(createdAt.plus(30, ChronoUnit.DAYS))
                    .createdAt(createdAt)
                    .updatedAt(getRandomInstantAfter(createdAt))
                    .user(randomUser)
                    .build();

            accounts.add(account);
        }

        oAuthAccountRepository.saveAll(accounts);
    }

    private void createAnalysisResults(List<ShortUrl> shortUrls, int count) {
        List<AnalysisResult> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ShortUrl randomShortUrl = shortUrls.get(random.nextInt(shortUrls.size()));
            AnalysisStatus status = getRandomAnalysisStatus();

            AnalysisResult result = AnalysisResult.builder()
                    .status(status)
                    .analysisEngine(random.nextDouble() < 0.8 ? "GOOGLE_SAFE_BROWSING" : "VIRUS_TOTAL")
                    .threatType(status == AnalysisStatus.SAFE ? "NONE" : getRandomThreatType())
                    .platformType(getRandomPlatformType())
                    .cacheDuration(random.nextDouble() < 0.5 ? "3600s" : null)
                    .details(status != AnalysisStatus.SAFE ? "Threat detected: " + getRandomThreatType() : null)
                    .analyzedAt(getRandomInstantBetween(2021, 2024))
                    .shortUrl(randomShortUrl)
                    .build();

            results.add(result);
        }

        analysisResultRepository.saveAll(results);
    }

    private void createClickLogsAndStats(List<ShortUrl> shortUrls, int clickLogCount) {
        List<ClickLog> clickLogs = new ArrayList<>();
        Map<String, ClickStat> clickStatsMap = new HashMap<>();
        Map<String, DimensionStat> dimensionStatsMap = new HashMap<>();

        for (int i = 0; i < clickLogCount; i++) {
            ShortUrl randomShortUrl = shortUrls.get(random.nextInt(shortUrls.size()));
            Instant timestamp = getRandomInstantBetween(2021, 2024);
            String country = getRandomElement(countries);
            String city = getRandomElement(cities);
            String browser = getRandomElement(browsers);
            String os = getRandomElement(operatingSystems);
            String deviceType = getRandomElement(deviceTypes);

            ClickLog clickLog = ClickLog.builder()
                    .ipVersion(random.nextDouble() < 0.9 ? IpVersion.IPV4 : IpVersion.IPV6)
                    .ipAddress(generateRandomIp())
                    .ipNormalized(generateRandomIp())
                    .subnet(generateRandomSubnet())
                    .userAgent(getRandomElement(userAgents))
                    .referrer(random.nextDouble() < 0.6 ? "https://" + getRandomElement(domains) : null)
                    .country(country)
                    .city(city)
                    .browser(browser)
                    .os(os)
                    .deviceType(deviceType)
                    .timestamp(timestamp)
                    .shortUrl(randomShortUrl)
                    .build();

            clickLogs.add(clickLog);

            for (Granularity granularity : Granularity.values()) {
                Instant bucket = getBucketStart(timestamp, granularity);
                String statsKey = randomShortUrl.getId() + "_" + granularity + "_" + bucket.toString();

                clickStatsMap.computeIfAbsent(statsKey, k -> ClickStat.builder()
                                .granularity(granularity)
                                .bucket(bucket)
                                .totalClicks(0L)
                                .shortUrl(randomShortUrl)
                                .build())
                        .setTotalClicks(clickStatsMap.get(statsKey).getTotalClicks() + 1);
            }

            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.COUNTRY, country);
            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.CITY, city);
            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.BROWSER, browser);
            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.OS, os);
            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.DEVICE_TYPE, deviceType);

            if (i % 1000 == 0) {
                clickLogRepository.saveAll(clickLogs);
                clickStatRepository.saveAll(clickStatsMap.values());
                dimensionStatRepository.saveAll(dimensionStatsMap.values());

                clickLogs.clear();
                clickStatsMap.clear();
                dimensionStatsMap.clear();

                log.info("Saved batch {} of click logs and stats", i / 1000 + 1);
            }
        }

        if (!clickLogs.isEmpty()) {
            clickLogRepository.saveAll(clickLogs);
            clickStatRepository.saveAll(clickStatsMap.values());
            dimensionStatRepository.saveAll(dimensionStatsMap.values());
        }
    }

    private void createDimensionStat(Map<String, DimensionStat> dimensionStatsMap, ShortUrl shortUrl, DimensionType type, String value) {
        String key = shortUrl.getId() + "_" + type + "_" + value;
        dimensionStatsMap.computeIfAbsent(key, k -> DimensionStat.builder()
                        .type(type)
                        .value(value)
                        .totalClicks(0L)
                        .shortUrl(shortUrl)
                        .build())
                .setTotalClicks(dimensionStatsMap.get(key).getTotalClicks() + 1);
    }

    private Instant getRandomInstantBetween(int startYear, int endYear) {
        LocalDateTime start = LocalDateTime.of(startYear, 1, 1, 0, 0);
        LocalDateTime end = LocalDateTime.of(endYear, 12, 31, 23, 59);
        long startEpoch = start.toEpochSecond(ZoneOffset.UTC);
        long endEpoch = end.toEpochSecond(ZoneOffset.UTC);
        long randomEpoch = ThreadLocalRandom.current().nextLong(startEpoch, endEpoch);
        return Instant.ofEpochSecond(randomEpoch);
    }

    private Instant getRandomInstantAfter(Instant after) {
        return after.plus(ThreadLocalRandom.current().nextLong(1, 365), ChronoUnit.DAYS);
    }

    private String generateRandomShortCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    private String generateRandomIp() {
        return random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256) + "." + random.nextInt(256);
    }

    private String generateRandomSubnet() {
        return generateRandomIp() + "/24";
    }

    private Instant getBucketStart(Instant timestamp, Granularity granularity) {
        LocalDateTime dateTime = LocalDateTime.ofInstant(timestamp, ZoneOffset.UTC);
        return switch (granularity) {
            case HOURLY -> dateTime.withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);
            case DAILY -> dateTime.withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);
            case MONTHLY ->
                    dateTime.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);
            case YEARLY ->
                    dateTime.withDayOfYear(1).withHour(0).withMinute(0).withSecond(0).withNano(0).toInstant(ZoneOffset.UTC);
        };
    }

    private <T> T getRandomElement(List<T> list) {
        return list.get(random.nextInt(list.size()));
    }

    private OAuthProvider getRandomOAuthProvider() {
        OAuthProvider[] providers = {OAuthProvider.GOOGLE, OAuthProvider.GITHUB};
        return providers[random.nextInt(providers.length)];
    }

    private ShortUrlStatus getRandomShortUrlStatus() {
        return random.nextDouble() < 0.8 ? ShortUrlStatus.ENABLED :
                random.nextDouble() < 0.9 ? ShortUrlStatus.DISABLED : ShortUrlStatus.DELETED;
    }

    private TokenType getRandomTokenType() {
        TokenType[] types = TokenType.values();
        return types[random.nextInt(types.length)];
    }

    private AnalysisStatus getRandomAnalysisStatus() {
        return random.nextDouble() < 0.7 ? AnalysisStatus.SAFE :
                random.nextDouble() < 0.9 ? AnalysisStatus.SUSPICIOUS : AnalysisStatus.MALICIOUS;
    }

    private String getRandomThreatType() {
        String[] threats = {"MALWARE", "SOCIAL_ENGINEERING", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION"};
        return threats[random.nextInt(threats.length)];
    }

    private String getRandomPlatformType() {
        String[] platforms = {"WINDOWS", "LINUX", "ANDROID", "OSX", "IOS", "ANY_PLATFORM"};
        return platforms[random.nextInt(platforms.length)];
    }
}