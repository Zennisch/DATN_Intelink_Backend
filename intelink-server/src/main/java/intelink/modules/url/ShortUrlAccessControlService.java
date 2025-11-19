package intelink.modules.url;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

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

}
