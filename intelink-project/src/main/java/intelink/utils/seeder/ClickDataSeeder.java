package intelink.utils.seeder;

import intelink.models.ClickLog;
import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.models.enums.IpVersion;
import intelink.repositories.ClickLogRepository;
import intelink.repositories.ClickStatRepository;
import intelink.repositories.DimensionStatRepository;
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
    private final DataSeedingUtils utils;

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

            ClickLog clickLog = ClickLog.builder()
                    .ipVersion(utils.getRandom().nextDouble() < 0.9 ? IpVersion.IPv4 : IpVersion.IPv6)
                    .ipAddress(utils.generateRandomIp())
                    .userAgent(utils.getRandomElement(utils.userAgents))
                    .referrer(utils.getRandom().nextDouble() < 0.6 ? "https://" + utils.getRandomElement(utils.domains) : null)
                    .timestamp(timestamp)
                    .shortUrl(randomShortUrl)
                    .build();

            clickLogs.add(clickLog);

            for (Granularity granularity : Granularity.values()) {
                Instant bucket = utils.getBucketStart(timestamp, granularity);
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

            if ((i + 1) % batchSize == 0 || i == clickLogCount - 1) {
                clickLogRepository.saveAll(clickLogs);
                clickStatRepository.saveAll(clickStatsMap.values());
                dimensionStatRepository.saveAll(dimensionStatsMap.values());

                clickLogs.clear();
                clickStatsMap.clear();
                dimensionStatsMap.clear();

                log.info("Saved batch {} of {}", (i / batchSize) + 1, (clickLogCount + batchSize - 1) / batchSize);
            }
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
}
