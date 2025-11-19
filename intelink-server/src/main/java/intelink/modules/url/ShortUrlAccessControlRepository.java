package intelink.modules.url;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ShortUrlAccessControlRepository extends JpaRepository<ShortUrlAccessControl, Long> {
    List<ShortUrlAccessControl> findByShortUrl(ShortUrl shortUrl);
}
