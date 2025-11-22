package intelink.modules.redirect.repositories;

import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DimensionStatRepository extends JpaRepository<DimensionStat, UUID> {
    Optional<DimensionStat> findByShortUrlAndTypeAndValue(ShortUrl shortUrl, DimensionType type, String value);
}
