package intelink.repositories;

import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DimensionStatRepository extends JpaRepository<DimensionStat, UUID> {
    Optional<DimensionStat> findByShortUrlAndTypeAndValue(ShortUrl shortUrl, DimensionType type, String value);

    List<DimensionStat> findByShortUrlAndType(ShortUrl shortUrl, DimensionType type);

    @Query("SELECT ds FROM DimensionStat ds WHERE ds.shortUrl = :shortUrl AND ds.type = :type ORDER BY ds.totalClicks DESC")
    List<DimensionStat> findByShortUrlAndTypeOrderByTotalClicksDesc(@Param("shortUrl") ShortUrl shortUrl, @Param("type") DimensionType type);

    List<DimensionStat> findByType(DimensionType type);

    List<DimensionStat> findByTypeAndShortUrl_ShortCodeIn(DimensionType type, Collection<String> shortUrlShortCodes);
}
