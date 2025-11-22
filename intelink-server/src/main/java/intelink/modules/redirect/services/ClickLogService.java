package intelink.modules.redirect.services;

import intelink.models.ClickLog;
import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.ClickStatus;
import intelink.models.enums.DimensionType;
import intelink.modules.redirect.repositories.ClickLogRepository;
import intelink.modules.url.services.ShortUrlService;
import intelink.utils.GeoLiteUtil;
import intelink.utils.IpUtil;
import intelink.utils.UserAgentUtil;
import intelink.utils.helper.DimensionEntry;
import intelink.utils.helper.IpInfo;
import intelink.utils.helper.UserAgentInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickLogService {

    private final ClickLogRepository clickLogRepository;
    private final ShortUrlService shortUrlService;
    private final DimensionStatService dimensionStatService;
    private final ClickStatService clickStatService;

    @Async("clickLogExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> recordClick(ShortUrl shortUrl, ClickStatus status, String reason, HttpServletRequest request) {
        try {
            IpInfo ipInfo = IpUtil.process(request);
            String ipAddress = ipInfo.ipAddress();
            String userAgent = request.getHeader("User-Agent");
            String referrer = request.getHeader("Referer");

            String countryCode = GeoLiteUtil.getCountryIsoFromIp(ipAddress);
            String city = GeoLiteUtil.getCityFromIp(ipAddress);

            switch (status) {
                case ALLOWED -> {
                    boolean isUniqueClick = !clickLogRepository.existsByShortUrlAndIpAddressAndUserAgent(shortUrl, ipAddress, userAgent);
                    shortUrlService.incrementAllowedCounters(shortUrl.getId(), isUniqueClick ? 1 : 0);
                }
                case BLOCKED -> shortUrlService.incrementBlockedCounters(shortUrl.getId());
            }

            UserAgentInfo userAgentInfo = UserAgentUtil.parseUserAgent(userAgent);
            List<DimensionEntry> dimensionEntries = getDimensionEntries(userAgentInfo, countryCode, city);
            recordDimensionStats(shortUrl, dimensionEntries, status);


            ClickLog clickLog = ClickLog.builder()
                    .shortUrl(shortUrl)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .status(status)
                    .reason(reason)
                    .build();
            clickLogRepository.save(clickLog);
        } catch (Exception e) {
            log.error("[ClickLogService.recordClick] Error recording {} click for ShortUrl ID {}: {}", status, shortUrl.getId(), e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    private static List<DimensionEntry> getDimensionEntries(UserAgentInfo userAgentInfo, String countryCode, String city) {
        String browser = userAgentInfo != null ? userAgentInfo.browser() : null;
        String os = userAgentInfo != null ? userAgentInfo.os() : null;
        String deviceType = userAgentInfo != null ? userAgentInfo.deviceType() : null;

        return List.of(
                new DimensionEntry(DimensionType.COUNTRY, countryCode),
                new DimensionEntry(DimensionType.CITY, city),
                new DimensionEntry(DimensionType.BROWSER, browser),
                new DimensionEntry(DimensionType.OS, os),
                new DimensionEntry(DimensionType.DEVICE_TYPE, deviceType
                ));
    }

    @Transactional
    public void recordDimensionStats(ShortUrl shortUrl, List<DimensionEntry> dimensionEntries, ClickStatus status) {
        dimensionEntries.forEach(entry -> {
            if (entry.value() == null || entry.value().isBlank()) {
                log.debug("[ClickLogService.recordDimensionStats] Skipping empty dimension value for ShortUrl ID {}: {}", shortUrl.getId(), entry.type());
                return;
            }
            DimensionStat dimensionStat = dimensionStatService
                    .findByShortUrlAndTypeAndValue(shortUrl, entry.type(), entry.value())
                    .orElseGet(() -> DimensionStat.builder()
                            .shortUrl(shortUrl)
                            .type(entry.type())
                            .value(entry.value())
                            .build());
            if (status == ClickStatus.ALLOWED) {
                dimensionStat.setTotalClicks(dimensionStat.getTotalClicks() + 1);
                dimensionStat.setAllowedClicks(dimensionStat.getAllowedClicks() + 1);
            } else if (status == ClickStatus.BLOCKED) {
                dimensionStat.setTotalClicks(dimensionStat.getTotalClicks() + 1);
                dimensionStat.setBlockedClicks(dimensionStat.getBlockedClicks() + 1);
            }
            dimensionStatService.save(dimensionStat);
        });
    }
}
