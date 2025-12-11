package intelink.modules.url.repositories;

import intelink.models.ShortUrl;
import intelink.models.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    Optional<ShortUrl> findByShortCodeAndUser(String shortCode, User user);

    Page<ShortUrl> findByUserAndDeletedAtIsNull(User user, Pageable pageable);

    @Query("SELECT s FROM ShortUrl s WHERE s.user = :user AND s.deletedAt IS NULL " +
            "AND (LOWER(s.title) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.originalUrl) LIKE LOWER(CONCAT('%', :query, '%')) " +
            "OR LOWER(s.shortCode) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<ShortUrl> searchByUserAndQuery(@Param("user") User user, @Param("query") String query, Pageable pageable);

    @Modifying
    @Query("UPDATE ShortUrl s SET s.totalClicks = s.totalClicks + 1, s.allowedClicks = s.allowedClicks + 1, s.uniqueClicks = s.uniqueClicks + :uniqueIncrement WHERE s.id = :id")
    void increaseAllowedCounters(@Param("id") Long id, @Param("uniqueIncrement") int uniqueIncrement);

    @Modifying
    @Query("UPDATE ShortUrl s SET s.totalClicks = s.totalClicks + 1, s.blockedClicks = s.blockedClicks + 1 WHERE s.id = :id")
    void increaseBlockedCounters(@Param("id") Long id);

    Optional<ShortUrl> findById(Long id);
}
