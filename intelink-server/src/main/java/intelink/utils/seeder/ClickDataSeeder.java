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

    public void seed() {
        if (clickLogRepository.count() == 0) {
            log.info("Starting Click Data Seeder...");
            List<ShortUrl> shortUrls = shortUrlRepository.findAll();
            if (shortUrls.isEmpty()) {
                log.warn("No short URLs found. Skipping click data seeding.");
                return;
            }
            createClickLogsAndStats(shortUrls, 50000);
            log.info("Click Data Seeder completed.");
        }
    }

    public void createClickLogsAndStats(List<ShortUrl> shortUrls, int clickLogCount) {
        log.info("Creating {} click logs and statistics...", clickLogCount);

        int clicksPerBatch = 500;
        int delaySeconds = 10;
        int totalBatches = (int) Math.ceil((double) clickLogCount / clicksPerBatch);
        
        log.info("Will seed {} clicks in {} batches of {} clicks with {} second delay", 
                clickLogCount, totalBatches, clicksPerBatch, delaySeconds);

        Map<String, ClickStat> clickStatsMap = new HashMap<>();
        Map<String, DimensionStat> dimensionStatsMap = new HashMap<>();

        int saveBatchSize = 1000;
        int processedClicks = 0;
        
        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int clicksInThisBatch = Math.min(clicksPerBatch, clickLogCount - processedClicks);
            List<ClickLog> clickLogs = new ArrayList<>();
            
            log.info("Processing batch {}/{} ({} clicks)...", batchNum + 1, totalBatches, clicksInThisBatch);
            
            for (int i = 0; i < clicksInThisBatch; i++) {
                int totalIndex = processedClicks + i;
                ShortUrl randomShortUrl = utils.getRandomElement(shortUrls);
                Instant timestamp = utils.getRandomInstantBetween(2025, 2025);
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
            }
            
            // Save click logs for this batch
            clickLogRepository.saveAll(clickLogs);
            processedClicks += clicksInThisBatch;
            log.info("Saved {} click logs (Total: {}/{})", clicksInThisBatch, processedClicks, clickLogCount);
            clickLogs.clear();
            
            // Wait before processing next batch (except for the last batch)
            if (batchNum < totalBatches - 1) {
                try {
                    log.info("Waiting {} seconds before next batch...", delaySeconds);
                    Thread.sleep(delaySeconds * 1000L);
                } catch (InterruptedException e) {
                    log.error("Thread interrupted during seeding", e);
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }

        // Save Stats in batches
        saveStatsInBatches(new ArrayList<>(clickStatsMap.values()), saveBatchSize);
        saveDimensionStatsInBatches(new ArrayList<>(dimensionStatsMap.values()), saveBatchSize);
        
        clickStatsMap.clear();
        dimensionStatsMap.clear();
        
        log.info("Completed seeding {} click logs with statistics", processedClicks);
    }

    private void saveStatsInBatches(List<ClickStat> stats, int batchSize) {
        int total = stats.size();
        log.info("Saving {} click stats...", total);
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            clickStatRepository.saveAll(stats.subList(i, end));
            log.info("Saved click stats batch {}/{}", (i / batchSize) + 1, (total + batchSize - 1) / batchSize);
        }
    }

    private void saveDimensionStatsInBatches(List<DimensionStat> stats, int batchSize) {
        int total = stats.size();
        log.info("Saving {} dimension stats...", total);
        for (int i = 0; i < total; i += batchSize) {
            int end = Math.min(i + batchSize, total);
            dimensionStatRepository.saveAll(stats.subList(i, end));
            log.info("Saved dimension stats batch {}/{}", (i / batchSize) + 1, (total + batchSize - 1) / batchSize);
        }
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
