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

    @Modifying
    @Query(value = """
        MERGE dimension_stats WITH (HOLDLOCK) AS target
        USING (SELECT :shortUrlId AS short_url_id, :type AS type, :value AS value) AS source
        ON target.short_url_id = source.short_url_id AND target.type = source.type AND target.value = source.value
        WHEN MATCHED THEN
            UPDATE SET 
                total_clicks = total_clicks + 1,
                allowed_clicks = CASE WHEN :isAllowed = 1 THEN allowed_clicks + 1 ELSE allowed_clicks END,
                blocked_clicks = CASE WHEN :isAllowed = 0 THEN blocked_clicks + 1 ELSE blocked_clicks END
        WHEN NOT MATCHED THEN
            INSERT (id, short_url_id, type, value, total_clicks, allowed_clicks, blocked_clicks)
            VALUES (NEWID(), :shortUrlId, :type, :value, 1, 
                    CASE WHEN :isAllowed = 1 THEN 1 ELSE 0 END,
                    CASE WHEN :isAllowed = 0 THEN 1 ELSE 0 END);
        """, nativeQuery = true)
    void upsertAndIncrement(@Param("shortUrlId") Long shortUrlId, 
                           @Param("type") String type, 
                           @Param("value") String value, 
                           @Param("isAllowed") int isAllowed);
}
