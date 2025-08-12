package intelink.services;

import intelink.dto.helper.DimensionInfo;
import intelink.models.ClickStat;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import intelink.models.enums.Granularity;
import intelink.repositories.ClickStatRepository;
import intelink.repositories.DimensionStatRepository;
import intelink.services.interfaces.IAnalyticService;
import intelink.utils.DateTimeUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyticService implements IAnalyticService {

    private final DimensionStatRepository dimensionStatRepository;
    private final ClickStatRepository clickStatRepository;
    private final ShortUrlService shortUrlService;

    private static <K, V> Map.Entry<K, V> entry(K k, V v) {
        return new AbstractMap.SimpleEntry<>(k, v);
    }

    @Transactional(readOnly = true)
    public void recordDimensionStats(String shortCode, DimensionInfo dimensionInfo) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("AnalyticsService.recordDimensionStats: Short code not found: " + shortCode));

        Map<DimensionType, String> dimensions = Stream.of(
                        entry(DimensionType.COUNTRY, dimensionInfo.getCountry()),
                        entry(DimensionType.CITY, dimensionInfo.getCity()),
                        entry(DimensionType.BROWSER, dimensionInfo.getBrowser()),
                        entry(DimensionType.OS, dimensionInfo.getOs()),
                        entry(DimensionType.DEVICE_TYPE, dimensionInfo.getDeviceType())
                )
                .filter(e -> e.getValue() != null && !e.getValue().isBlank())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        dimensions.forEach((type, value) -> {
            if (value == null || value.isBlank()) {
                log.warn("AnalyticsService.recordDimensionStats: Dimension {} is null or blank for short code {}", type, shortCode);
                return;
            }
            DimensionStat dimensionStat = dimensionStatRepository
                    .findByShortUrlAndTypeAndValue(shortUrl, type, value)
                    .orElseGet(() -> {
                        log.info("Creating new DimensionStat for short code: {}, type: {}, value: {}", shortCode, type, value);
                        return DimensionStat.builder()
                                .shortUrl(shortUrl)
                                .type(type)
                                .value(value)
                                .totalClicks(0L)
                                .build();
                    });
            dimensionStat.setTotalClicks(dimensionStat.getTotalClicks() + 1);
            dimensionStatRepository.save(dimensionStat);
        });
    }

    @Transactional
    public void recordClickStats(String shortCode) {
        ShortUrl shortUrl = shortUrlService.findByShortCode(shortCode)
                .orElseThrow(() -> new IllegalArgumentException("AnalyticsService.recordClickStats: Short code not found: " + shortCode));

        Instant now = Instant.now();
        for (Granularity granularity : Granularity.values()) {
            Instant bucket = DateTimeUtil.getBucketStart(now, granularity);
            ClickStat clickStat = clickStatRepository
                    .findByShortUrlAndGranularityAndBucket(shortUrl, granularity, bucket)
                    .orElseGet(() -> {
                        log.info("Creating new ClickStat for short code: {}, granularity: {}, bucket: {}", shortCode, granularity, bucket);
                        return ClickStat.builder()
                                .shortUrl(shortUrl)
                                .granularity(granularity)
                                .bucket(bucket)
                                .totalClicks(0L)
                                .build();
                    });
            clickStat.setTotalClicks(clickStat.getTotalClicks() + 1);
            clickStatRepository.save(clickStat);
        }
    }
}
