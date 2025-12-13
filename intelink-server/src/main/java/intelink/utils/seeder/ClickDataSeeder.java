package intelink.utils.seeder;

import intelink.models.ClickLog;
import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.ClickStatus;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.models.enums.IPVersion;
import intelink.modules.redirect.repositories.ClickLogRepository;
import intelink.modules.redirect.repositories.ClickStatRepository;
import intelink.modules.redirect.repositories.DimensionStatRepository;
import intelink.modules.url.repositories.ShortUrlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClickDataSeeder {

    private final ClickLogRepository clickLogRepository;
    private final ClickStatRepository clickStatRepository;
    private final DimensionStatRepository dimensionStatRepository;
    private final ShortUrlRepository shortUrlRepository;
    private final DataSeedingUtils utils;

    @Transactional
    public void seed() {
        if (clickLogRepository.count() == 0) {
            List<ShortUrl> shortUrls = shortUrlRepository.findAll();
            if (shortUrls.isEmpty()) {
                return;
            }
            createClickLogsAndStats(shortUrls, 50000);
        }
    }

    public void createClickLogsAndStats(List<ShortUrl> shortUrls, int clickLogCount) {
        log.info("Creating {} click logs and statistics...", clickLogCount);

        List<ClickLog> clickLogs = new ArrayList<>();
        Map<String, ClickStat> clickStatsMap = new HashMap<>();
        Map<String, DimensionStat> dimensionStatsMap = new HashMap<>();

        int batchSize = 1000;
        for (int i = 0; i < clickLogCount; i++) {
            ShortUrl randomShortUrl = utils.getRandomElement(shortUrls);
            Instant timestamp = utils.getRandomInstantBetween(2024, 2025);
            String country = utils.getRandomElement(utils.countries);
            String city = utils.getRandomElement(utils.cities);
            String browser = utils.getRandomElement(utils.browsers);
            String os = utils.getRandomElement(utils.operatingSystems);
            String deviceType = utils.getRandomElement(utils.deviceTypes);
            ClickStatus status = utils.getRandom().nextDouble() < 0.9 ? ClickStatus.ALLOWED : ClickStatus.BLOCKED;

            ClickLog clickLog = ClickLog.builder()
                    .ipVersion(IPVersion.IPV4)
                    .ipAddress(utils.generateRandomIp())
                    .userAgent(utils.getRandomElement(utils.userAgents))
                    .referrer(utils.getRandom().nextDouble() < 0.6 ? "https://" + utils.getRandomElement(utils.domains) : null)
                    .timestamp(timestamp)
                    .shortUrl(randomShortUrl)
                    .status(status)
                    .build();

            clickLogs.add(clickLog);

            // Update ShortUrl counters (in memory for now, but ideally should update DB)
            // Since we are seeding, we can update ShortUrl at the end or just let it be inconsistent if not critical
            // But let's try to be consistent if possible. However, updating ShortUrl 50k times is slow.
            // We will skip updating ShortUrl counters for performance in this batch seeder, 
            // or we could aggregate and update at the end. For now, let's focus on logs and stats.

            for (Granularity granularity : Granularity.values()) {
                Instant bucketStart = utils.getBucketStart(timestamp, granularity);
                Instant bucketEnd = utils.getBucketEnd(bucketStart, granularity);
                String statsKey = randomShortUrl.getId() + "_" + granularity + "_" + bucketStart.toString();

                clickStatsMap.computeIfAbsent(statsKey, k -> ClickStat.builder()
                                .granularity(granularity)
                                .bucketStart(bucketStart)
                                .bucketEnd(bucketEnd)
                                .totalClicks(0L)
                                .allowedClicks(0L)
                                .blockedClicks(0L)
                                .shortUrl(randomShortUrl)
                                .build());
                
                ClickStat stat = clickStatsMap.get(statsKey);
                stat.setTotalClicks(stat.getTotalClicks() + 1);
                if (status == ClickStatus.ALLOWED) {
                    stat.setAllowedClicks(stat.getAllowedClicks() + 1);
                } else {
                    stat.setBlockedClicks(stat.getBlockedClicks() + 1);
                }
            }

            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.COUNTRY, country, status);
            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.CITY, city, status);
            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.BROWSER, browser, status);
            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.OS, os, status);
            createDimensionStat(dimensionStatsMap, randomShortUrl, DimensionType.DEVICE_TYPE, deviceType, status);

            if ((i + 1) % batchSize == 0 || i == clickLogCount - 1) {
                clickLogRepository.saveAll(clickLogs);

                if (i == clickLogCount - 1) {
                    clickStatRepository.saveAll(clickStatsMap.values());
                    dimensionStatRepository.saveAll(dimensionStatsMap.values());
                    clickStatsMap.clear();
                    dimensionStatsMap.clear();
                }

                clickLogs.clear();

                log.info("Saved batch {} of {}", (i / batchSize) + 1, (clickLogCount + batchSize - 1) / batchSize);
            }
        }
        
        // Update ShortUrl total clicks
        // This is a simplified update, assuming all clicks are allowed and unique logic is ignored for seeding speed
        // for (ShortUrl url : shortUrls) {
             // We could count from logs but that's expensive. 
             // For seeding, we might just leave ShortUrl counters as is or update them if needed.
             // Given the requirement is about logs and stats, I'll leave ShortUrl counters alone to avoid complexity/performance issues.
        // }
    }

    private void createDimensionStat(Map<String, DimensionStat> dimensionStatsMap, ShortUrl shortUrl, DimensionType type, String value, ClickStatus status) {
        String key = shortUrl.getId() + "_" + type + "_" + value;
        dimensionStatsMap.computeIfAbsent(key, k -> DimensionStat.builder()
                        .type(type)
                        .value(value)
                        .totalClicks(0L)
                        .allowedClicks(0L)
                        .blockedClicks(0L)
                        .shortUrl(shortUrl)
                        .build());
        
        DimensionStat stat = dimensionStatsMap.get(key);
        stat.setTotalClicks(stat.getTotalClicks() + 1);
        if (status == ClickStatus.ALLOWED) {
            stat.setAllowedClicks(stat.getAllowedClicks() + 1);
        } else {
            stat.setBlockedClicks(stat.getBlockedClicks() + 1);
        }
    }
}
