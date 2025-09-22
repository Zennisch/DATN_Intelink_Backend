package intelink.services;

import intelink.dto.object.DimensionInfo;
import intelink.dto.object.IpProcessResult;
import intelink.dto.object.UserAgentInfo;
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
import intelink.services.interfaces.IClickLogService;
import intelink.services.interfaces.IRedirectService;
import intelink.services.interfaces.IShortUrlService;
import intelink.utils.DateTimeUtil;
import intelink.utils.GeoLiteUtil;
import intelink.utils.IpUtil;
import intelink.utils.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickLogService implements IClickLogService {

    private final ClickLogRepository clickLogRepository;
    private final IShortUrlService shortUrlService;
    private final DimensionStatRepository dimensionStatRepository;
    private final ClickStatRepository clickStatRepository;

    @Transactional
    public ClickLog record(String shortCode, HttpServletRequest request) {
        IpProcessResult ipProcessResult = IpUtil.process(request);
        IpVersion ipVersion = ipProcessResult.getIpVersion();
        String ipAddress = ipProcessResult.getIpAddress();
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        String country = GeoLiteUtil.getCountryFromIp(ipAddress);
        String city = GeoLiteUtil.getCityFromIp(ipAddress);

        UserAgentInfo userAgentInfo = UserAgentUtil.parseUserAgent(userAgent);

        Optional<ShortUrl> shortUrl = shortUrlService.findByShortCode(shortCode);
        if (shortUrl.isEmpty()) {
            log.warn("ClickLogService.record: Short URL not found for code: {}", shortCode);
            return null;
        }

        ClickLog clickLog = ClickLog.builder()
                .shortUrl(shortUrl.get())
                .ipAddress(ipAddress)
                .ipVersion(ipVersion)
                .userAgent(userAgent)
                .referrer(referrer)
                .build();


        shortUrlService.increaseTotalClicks(shortCode);

        DimensionInfo dimensionInfo = new DimensionInfo(
                country, city, userAgentInfo.getBrowser(), userAgentInfo.getOs(), userAgentInfo.getDeviceType()
        );

        recordDimensionStats(shortCode, dimensionInfo);
        recordClickStats(shortCode);

        clickLogRepository.save(clickLog);
        return clickLog;
    }

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
