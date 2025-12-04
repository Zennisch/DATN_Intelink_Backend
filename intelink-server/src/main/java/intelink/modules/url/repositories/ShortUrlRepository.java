package intelink.modules.url.repositories;

import intelink.models.ShortUrl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, UUID> {
    boolean existsByShortCode(String shortCode);

    Optional<ShortUrl> findByShortCode(String shortCode);

    @Modifying
    @Query("UPDATE ShortUrl s SET s.totalClicks = s.totalClicks + 1, s.allowedClicks = s.allowedClicks + 1, s.uniqueClicks = s.uniqueClicks + :uniqueIncrement WHERE s.id = :id")
    void increaseAllowedCounters(@Param("id") Long id, @Param("uniqueIncrement") int uniqueIncrement);

    @Modifying
    @Query("UPDATE ShortUrl s SET s.totalClicks = s.totalClicks + 1, s.blockedClicks = s.blockedClicks + 1 WHERE s.id = :id")
    void increaseBlockedCounters(@Param("id") Long id);

    Optional<ShortUrl> findById(Long id);
}
