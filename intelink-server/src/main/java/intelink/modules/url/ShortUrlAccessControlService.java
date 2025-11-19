package intelink.modules.url;

import intelink.models.ShortUrlAccessControl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlAccessControlService {

    private ShortUrlAccessControlRepository shortUrlAccessControlRepository;

    public ShortUrlAccessControl save(ShortUrlAccessControl accessControl) {
        return shortUrlAccessControlRepository.save(accessControl);
    }

}
