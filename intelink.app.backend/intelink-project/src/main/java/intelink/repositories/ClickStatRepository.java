package intelink.repositories;

import intelink.models.ClickStat;
import intelink.models.ShortUrl;
import intelink.models.enums.Granularity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface ClickStatRepository extends JpaRepository<ClickStat, Long> {
    Optional<ClickStat> findByShortUrlAndGranularityAndBucket(ShortUrl shortUrl, Granularity granularity, Instant bucket);
}
