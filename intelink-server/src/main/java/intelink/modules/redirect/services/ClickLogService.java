package intelink.modules.redirect.services;

import intelink.models.ClickLog;
import intelink.models.ShortUrl;
import intelink.models.enums.ClickStatus;
import intelink.modules.redirect.repositories.ClickLogRepository;
import intelink.modules.url.services.ShortUrlService;
import intelink.utils.IpUtil;
import intelink.utils.helper.IpProcessResult;
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
    public CompletableFuture<Void> recordAllowedClicks(ShortUrl shortUrl, HttpServletRequest request) {
        try {
            IpProcessResult ipProcessResult = IpUtil.process(request);
            String ipAddress = ipProcessResult.ipAddress();
            String userAgent = request.getHeader("User-Agent");
            String referrer = request.getHeader("Referer");

            boolean isUniqueClick = !clickLogRepository.existsByShortUrlAndIpAddressAndUserAgent(shortUrl, ipAddress, userAgent);
            shortUrlService.incrementAllowedCounters(shortUrl.getId(), isUniqueClick ? 1 : 0);

            ClickLog clickLog = ClickLog.builder()
                    .shortUrl(shortUrl)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .status(ClickStatus.ALLOWED)
                    .build();
            clickLogRepository.save(clickLog);
        } catch (Exception e) {
            log.error("[ClickLogService.recordAllowedClicks] Error recording allowed click for ShortUrl ID {}: {}", shortUrl.getId(), e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }

    @Async("clickLogExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompletableFuture<Void> recordBlockedClicks(ShortUrl shortUrl, HttpServletRequest request) {
        try {
            IpProcessResult ipProcessResult = IpUtil.process(request);
            String ipAddress = ipProcessResult.ipAddress();
            String userAgent = request.getHeader("User-Agent");
            String referrer = request.getHeader("Referer");

            shortUrlService.incrementBlockedCounter(shortUrl.getId());

            ClickLog clickLog = ClickLog.builder()
                    .shortUrl(shortUrl)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .referrer(referrer)
                    .status(ClickStatus.BLOCKED)
                    .build();
            clickLogRepository.save(clickLog);
        } catch (Exception e) {
            log.error("[ClickLogService.recordBlockedClicks] Error recording blocked click for ShortUrl ID {}: {}", shortUrl.getId(), e.getMessage());
        }
        return CompletableFuture.completedFuture(null);
    }
}
