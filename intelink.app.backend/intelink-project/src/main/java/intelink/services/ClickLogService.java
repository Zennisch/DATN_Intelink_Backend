package intelink.services;

import intelink.dto.helper.IpProcessResult;
import intelink.dto.helper.UserAgentInfo;
import intelink.models.ClickLog;
import intelink.models.ShortUrl;
import intelink.models.enums.IpVersion;
import intelink.repositories.ClickLogRepository;
import intelink.services.interfaces.IClickLogService;
import intelink.utils.GeoLiteUtil;
import intelink.utils.IpUtil;
import intelink.utils.UserAgentUtil;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickLogService implements IClickLogService {

    private final ClickLogRepository clickLogRepository;
    private final ShortUrlService shortUrlService;

    @Transactional
    public ClickLog record(String shortCode, HttpServletRequest request) {
        IpProcessResult ipProcessResult = IpUtil.process(request);
        IpVersion ipVersion = ipProcessResult.getIpVersion();
        String ipAddress = ipProcessResult.getIpAddress();
        String ipNormalized = ipProcessResult.getIpNormalized();
        String subnet = ipProcessResult.getSubnet();
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
                .ipNormalized(ipNormalized)
                .subnet(subnet)
                .userAgent(userAgent)
                .referrer(referrer)
                .country(country)
                .city(city)
                .browser(userAgentInfo.getBrowser())
                .os(userAgentInfo.getOs())
                .deviceType(userAgentInfo.getDeviceType())
                .build();

        clickLogRepository.save(clickLog);
        log.debug("Recorded click for short code: {} from IP: {}", shortCode, ipAddress);
        shortUrlService.incrementTotalClicks(shortCode);
        return clickLog;
    }
}
