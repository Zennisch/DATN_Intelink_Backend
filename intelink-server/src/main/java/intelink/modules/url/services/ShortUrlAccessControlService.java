package intelink.modules.url.services;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.enums.AccessControlType;
import intelink.modules.url.repositories.ShortUrlAccessControlRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlAccessControlService {

    private final ShortUrlAccessControlRepository shortUrlAccessControlRepository;

    public ShortUrlAccessControl save(ShortUrlAccessControl accessControl) {
        return shortUrlAccessControlRepository.save(accessControl);
    }

    public List<ShortUrlAccessControl> getShortUrlAccessControls(ShortUrl shortUrl) {
        return shortUrlAccessControlRepository.findByShortUrl(shortUrl);
    }

    public Optional<ShortUrlAccessControl> findByShortUrlAndType(ShortUrl shortUrl, AccessControlType accessControlType) {
        return shortUrlAccessControlRepository.findByShortUrlAndType(shortUrl, accessControlType);
    }

    public List<ShortUrlAccessControl> findAllByShortUrlAndType(ShortUrl shortUrl, AccessControlType accessControlType) {
        return shortUrlAccessControlRepository.findAllByShortUrlAndType(shortUrl, accessControlType);
    }

}
