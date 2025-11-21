package intelink.modules.redirect.services;

import intelink.models.ClickLog;
import intelink.models.ShortUrl;
import intelink.models.enums.ClickStatus;
import intelink.modules.redirect.repositories.ClickLogRepository;
import intelink.modules.url.services.ShortUrlService;
import intelink.utils.GeoLiteUtil;
import intelink.utils.IpUtil;
import intelink.utils.UserAgentUtil;
import intelink.utils.helper.DimensionInfo;
import intelink.utils.helper.IpInfo;
import intelink.utils.helper.UserAgentInfo;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickLogService {

    private final ClickLogRepository clickLogRepository;
    private final ShortUrlService shortUrlService;

    @Async("clickLogExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> recordClick(ShortUrl shortUrl, HttpServletRequest request, ClickStatus status, String reason) {
        try {
            IpInfo ipInfo = IpUtil.process(request);
            String ipAddress = ipInfo.ipAddress();
            String userAgent = request.getHeader("User-Agent");
            String referrer = request.getHeader("Referer");

            String country = GeoLiteUtil.getCountryNameFromIp(ipAddress);
            String countryCode = GeoLiteUtil.getCountryIsoFromIp(ipAddress);
            String city = GeoLiteUtil.getCityFromIp(ipAddress);

            switch (status) {
                case ALLOWED -> {
                    boolean isUniqueClick = !clickLogRepository.existsByShortUrlAndIpAddressAndUserAgent(shortUrl, ipAddress, userAgent);
                    shortUrlService.incrementAllowedCounters(shortUrl.getId(), isUniqueClick ? 1 : 0);
                }
                case BLOCKED -> shortUrlService.incrementBlockedCounter(shortUrl.getId());
            }

            UserAgentInfo userAgentInfo = UserAgentUtil.parseUserAgent(userAgent);
            String browser = userAgentInfo != null ? userAgentInfo.browser() : null;
            String os = userAgentInfo != null ? userAgentInfo.os() : null;
            String deviceType = userAgentInfo != null ? userAgentInfo.deviceType() : null;

            DimensionInfo dimensionInfo = new DimensionInfo(
                    country,
                    countryCode,
                    city,
                    browser,
                    os,
                    deviceType
            );

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
}
