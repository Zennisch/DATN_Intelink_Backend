package intelink.modules.redirect.services;

import intelink.models.ShortUrl;
import intelink.modules.redirect.repositories.ClickLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickLogService {

    private final ClickLogRepository clickLogRepository;

    public boolean existsByShortUrlAndIpAddressAndUserAgent(ShortUrl shortUrl, String ipAddress, String userAgent) {
        return clickLogRepository.existsByShortUrlAndIpAddressAndUserAgent(shortUrl, ipAddress, userAgent);
    }
}
