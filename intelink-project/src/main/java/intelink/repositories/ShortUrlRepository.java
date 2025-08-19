package intelink.repositories;

import intelink.models.ShortUrl;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ShortUrlRepository extends JpaRepository<ShortUrl, Long> {
    Optional<ShortUrl> findByShortCode(String shortCode);

    @Modifying
    @Query("UPDATE ShortUrl s SET s.totalClicks = s.totalClicks + 1 WHERE s.shortCode = :shortCode")
    void incrementTotalClicks(@Param("shortCode") String shortCode);

    Page<ShortUrl> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    
    Page<ShortUrl> findByUserId(Long userId, Pageable pageable);
}
