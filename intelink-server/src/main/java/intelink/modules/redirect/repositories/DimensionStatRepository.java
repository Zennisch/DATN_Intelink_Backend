package intelink.modules.redirect.repositories;

import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface DimensionStatRepository extends JpaRepository<DimensionStat, UUID> {
    Optional<DimensionStat> findByShortUrlAndTypeAndValue(ShortUrl shortUrl, DimensionType type, String value);

    @Modifying
    @Query("UPDATE DimensionStat d SET d.totalClicks = d.totalClicks + 1, d.allowedClicks = d.allowedClicks + 1 WHERE d.id = :id")
    void increaseAllowedCounters(@Param("id") UUID id);

    @Modifying
    @Query("UPDATE DimensionStat d SET d.totalClicks = d.totalClicks + 1, d.blockedClicks = d.blockedClicks + 1 WHERE d.id = :id")
    void increaseBlockedCounters(@Param("id") UUID id);
}
