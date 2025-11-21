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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickLogService {

    private final ClickLogRepository clickLogRepository;
    private final ShortUrlService shortUrlService;

    @Transactional
    public void recordAllowedClicks(ShortUrl shortUrl, HttpServletRequest request) {
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
    }

    @Transactional
    public void recordBlockedClicks(ShortUrl shortUrl, HttpServletRequest request) {
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
    }
}
