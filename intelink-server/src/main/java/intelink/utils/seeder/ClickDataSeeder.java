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
import java.util.List;

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

        int processedClicks = 0;
        
        for (int batchNum = 0; batchNum < totalBatches; batchNum++) {
            int clicksInThisBatch = Math.min(clicksPerBatch, clickLogCount - processedClicks);
            
            log.info("Processing batch {}/{} ({} clicks)...", batchNum + 1, totalBatches, clicksInThisBatch);
            
            for (int i = 0; i < clicksInThisBatch; i++) {
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

                clickLogRepository.save(clickLog);

                for (Granularity granularity : Granularity.values()) {
                    Instant bucketStart = utils.getBucketStart(timestamp, granularity);
                    Instant bucketEnd = utils.getBucketEnd(bucketStart, granularity);
                    
                    clickStatRepository.upsertAndIncrement(
                        randomShortUrl.getId(),
                        granularity.name(),
                        bucketStart,
                        bucketEnd,
                        status == ClickStatus.ALLOWED ? 1 : 0
                    );
                }

                updateDimensionStat(randomShortUrl, DimensionType.COUNTRY, country, status);
                updateDimensionStat(randomShortUrl, DimensionType.CITY, city, status);
                updateDimensionStat(randomShortUrl, DimensionType.BROWSER, browser, status);
                updateDimensionStat(randomShortUrl, DimensionType.OS, os, status);
                updateDimensionStat(randomShortUrl, DimensionType.DEVICE_TYPE, deviceType, status);
            }
            
            processedClicks += clicksInThisBatch;
            log.info("Processed {} clicks (Total: {}/{})", clicksInThisBatch, processedClicks, clickLogCount);
            
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
        
        log.info("Completed seeding {} click logs with statistics", processedClicks);
    }

    private void updateDimensionStat(ShortUrl shortUrl, DimensionType type, String value, ClickStatus status) {
        dimensionStatRepository.upsertAndIncrement(
            shortUrl.getId(),
            type.name(),
            value,
            status == ClickStatus.ALLOWED ? 1 : 0
        );
    }
}
