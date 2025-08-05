package intelink.repositories;

import intelink.models.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {

    Optional<ShortUrl> findByShortCode(String shortCode);

    @Query("SELECT s FROM ShortUrl s WHERE s.user.id = :userId ORDER BY s.createdAt DESC")
    Page<ShortUrl> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT s FROM ShortUrl s WHERE s.user.id = :userId AND s.isActive = true")
    List<ShortUrl> findActiveByUserId(@Param("userId") Long userId);

    @Query("SELECT s FROM ShortUrl s WHERE s.expiresAt IS NOT NULL AND s.expiresAt < :timestamp AND s.isActive = true")
    List<ShortUrl> findExpiredUrls(@Param("timestamp") Timestamp timestamp);

    @Query("SELECT s FROM ShortUrl s WHERE s.maxUsage IS NOT NULL AND s.totalClicks >= s.maxUsage AND s.isActive = true")
    List<ShortUrl> findMaxUsageReachedUrls();

    @Modifying
    @Query("UPDATE ShortUrl s SET s.totalClicks = s.totalClicks + 1 WHERE s.shortCode = :shortCode")
    void incrementTotalClicks(@Param("shortCode") String shortCode);

    @Modifying
    @Query("UPDATE ShortUrl s SET s.isActive = false WHERE s.id IN :ids")
    void deactivateUrls(@Param("ids") List<Long> ids);

    @Query("SELECT COUNT(s) FROM ShortUrl s WHERE s.user.id = :userId AND s.createdAt >= :since")
    long countByUserIdAndCreatedAtAfter(@Param("userId") Long userId, @Param("since") Instant since);
}