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
    public void recordAllowedClicks(String shortCode, HttpServletRequest request) {
        IpProcessResult ipProcessResult = IpUtil.process(request);
        String ipAddress = ipProcessResult.ipAddress();
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        ShortUrl shortUrl = shortUrlService.findByShortCodeOrNull(shortCode);
        boolean isUniqueClick = !clickLogRepository.existsByShortUrlAndIpAddressAndUserAgent(shortUrl, ipAddress, userAgent);
        shortUrl.setTotalClicks(shortUrl.getTotalClicks() + 1);
        shortUrl.setAllowedClicks(shortUrl.getAllowedClicks() + 1);
        if (isUniqueClick) {
            shortUrl.setUniqueClicks(shortUrl.getUniqueClicks() + 1);
        }

        shortUrlService.save(shortUrl);

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
    public void recordBlockedClicks(String shortCode, HttpServletRequest request) {
        IpProcessResult ipProcessResult = IpUtil.process(request);
        String ipAddress = ipProcessResult.ipAddress();
        String userAgent = request.getHeader("User-Agent");
        String referrer = request.getHeader("Referer");

        ShortUrl shortUrl = shortUrlService.findByShortCodeOrNull(shortCode);
        shortUrl.setTotalClicks(shortUrl.getTotalClicks() + 1);
        shortUrl.setBlockedClicks(shortUrl.getBlockedClicks() + 1);
        shortUrlService.save(shortUrl);

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
