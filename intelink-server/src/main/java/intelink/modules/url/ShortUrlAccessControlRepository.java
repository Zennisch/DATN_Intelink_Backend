package intelink.modules.url;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.enums.AccessControlType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlAccessControlRepository extends JpaRepository<ShortUrlAccessControl, Long> {
    List<ShortUrlAccessControl> findByShortUrl(ShortUrl shortUrl);

    Optional<ShortUrlAccessControl> findByShortUrlAndType(ShortUrl shortUrl, AccessControlType type);

    List<ShortUrlAccessControl> findAllByShortUrlAndType(ShortUrl shortUrl, AccessControlType type);
}
