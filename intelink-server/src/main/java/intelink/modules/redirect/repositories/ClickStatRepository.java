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

    @Modifying
    @Query(value = """
        MERGE click_stats WITH (HOLDLOCK) AS target
        USING (SELECT :shortUrlId AS short_url_id, :granularity AS granularity, 
                      :bucketStart AS bucket_start, :bucketEnd AS bucket_end) AS source
        ON target.short_url_id = source.short_url_id 
           AND target.granularity = source.granularity 
           AND target.bucket_start = source.bucket_start
        WHEN MATCHED THEN
            UPDATE SET 
                total_clicks = total_clicks + 1,
                allowed_clicks = CASE WHEN :isAllowed = 1 THEN allowed_clicks + 1 ELSE allowed_clicks END,
                blocked_clicks = CASE WHEN :isAllowed = 0 THEN blocked_clicks + 1 ELSE blocked_clicks END
        WHEN NOT MATCHED THEN
            INSERT (id, short_url_id, granularity, bucket_start, bucket_end, total_clicks, allowed_clicks, blocked_clicks)
            VALUES (NEWID(), :shortUrlId, :granularity, :bucketStart, :bucketEnd, 1,
                    CASE WHEN :isAllowed = 1 THEN 1 ELSE 0 END,
                    CASE WHEN :isAllowed = 0 THEN 1 ELSE 0 END);
        """, nativeQuery = true)
    void upsertAndIncrement(@Param("shortUrlId") Long shortUrlId,
                           @Param("granularity") String granularity,
                           @Param("bucketStart") Instant bucketStart,
                           @Param("bucketEnd") Instant bucketEnd,
                           @Param("isAllowed") int isAllowed);

    @Query("SELECT c FROM ClickStat c WHERE c.shortUrl = :shortUrl AND c.granularity = :granularity " +
           "AND c.bucketStart >= :from AND c.bucketStart < :to ORDER BY c.bucketStart ASC")
    java.util.List<ClickStat> findByShortUrlAndGranularityAndBucketStartBetween(
            @Param("shortUrl") ShortUrl shortUrl,
            @Param("granularity") Granularity granularity,
            @Param("from") Instant from,
            @Param("to") Instant to);
}
