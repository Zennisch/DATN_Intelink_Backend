package intelink.modules.redirect.repositories;

import intelink.models.ClickStat;
import intelink.models.ShortUrl;
import intelink.models.enums.Granularity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ClickStatRepository extends JpaRepository<ClickStat, UUID> {
    Optional<ClickStat> findByShortUrlAndGranularityAndBucketStart(ShortUrl shortUrl, Granularity granularity, Instant bucketStart);

    @Modifying
    @Query("UPDATE ClickStat c SET c.totalClicks = c.totalClicks + 1, c.allowedClicks = c.allowedClicks + 1 WHERE c.id = :id")
    void increaseAllowedCounters(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE ClickStat c SET c.totalClicks = c.totalClicks + 1, c.blockedClicks = c.blockedClicks + 1 WHERE c.id = :id")
    void increaseBlockedCounters(@Param("id") UUID id);
}
