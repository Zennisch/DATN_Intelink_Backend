package intelink.repositories;

import intelink.models.ClickStat;
import intelink.models.ShortUrl;
import intelink.models.enums.Granularity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface    ClickStatRepository extends JpaRepository<ClickStat, Long> {
    Optional<ClickStat> findByShortUrlAndGranularityAndBucket(ShortUrl shortUrl, Granularity granularity, Instant bucket);

    List<ClickStat> findByShortUrlAndGranularityOrderByBucketAsc(ShortUrl shortUrl, Granularity granularity);

    @Query("SELECT cs FROM ClickStat cs WHERE cs.shortUrl = :shortUrl AND cs.granularity = :granularity AND cs.bucket >= :startTime ORDER BY cs.bucket ASC")
    List<ClickStat> findByShortUrlAndGranularityAndBucketGreaterThanEqualOrderByBucketAsc(
            @Param("shortUrl") ShortUrl shortUrl,
            @Param("granularity") Granularity granularity,
            @Param("startTime") Instant startTime);

    List<ClickStat> findByShortUrlAndGranularityAndBucketGreaterThanEqualAndBucketLessThanEqualOrderByBucketAsc(
            ShortUrl shortUrl, Granularity granularity, Instant from, Instant to
    );

    List<ClickStat> findByGranularityAndBucketBetween(Granularity granularity, Instant bucketAfter, Instant bucketBefore);

    List<ClickStat> findByShortUrl_ShortCodeInAndGranularityAndBucketBetween(Collection<String> shortUrlShortCodes, Granularity granularity, Instant bucketAfter, Instant bucketBefore);

    List<ClickStat> findByShortUrl_ShortCodeInAndBucketBetween(Collection<String> shortUrlShortCodes, Instant bucketAfter, Instant bucketBefore);

    List<ClickStat> findByBucketBetween(Instant bucketAfter, Instant bucketBefore);
}