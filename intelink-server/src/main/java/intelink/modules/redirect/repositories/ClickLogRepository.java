package intelink.modules.redirect.repositories;

import intelink.models.ClickLog;
import intelink.models.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ClickLogRepository extends JpaRepository<ClickLog, UUID> {
    boolean existsByShortUrlAndIpAddressAndUserAgent(ShortUrl shortUrl, String ipAddress, String userAgent);
}
