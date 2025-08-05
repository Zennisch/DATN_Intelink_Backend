package intelink.repositories;

import intelink.models.ClickLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface ClickLogRepository extends JpaRepository<ClickLog, UUID> {

    @Query("SELECT c FROM ClickLog c WHERE c.shortCode = :shortCode ORDER BY c.timestamp DESC")
    Page<ClickLog> findByShortCodeOrderByTimestampDesc(@Param("shortCode") String shortCode, Pageable pageable);

    @Query("SELECT c FROM ClickLog c WHERE c.shortCode = :shortCode AND c.timestamp BETWEEN :startTime AND :endTime")
    List<ClickLog> findByShortCodeAndTimestampBetween(
            @Param("shortCode") String shortCode,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    @Query("SELECT COUNT(c) FROM ClickLog c WHERE c.shortCode = :shortCode")
    long countByShortCode(@Param("shortCode") String shortCode);

    @Query("SELECT COUNT(c) FROM ClickLog c WHERE c.shortCode = :shortCode AND c.timestamp BETWEEN :startTime AND :endTime")
    long countByShortCodeAndTimestampBetween(
            @Param("shortCode") String shortCode,
            @Param("startTime") Instant startTime,
            @Param("endTime") Instant endTime);

    @Query("SELECT c.country, COUNT(c) FROM ClickLog c WHERE c.shortCode = :shortCode AND c.country IS NOT NULL GROUP BY c.country ORDER BY COUNT(c) DESC")
    List<Object[]> countByShortCodeGroupByCountry(@Param("shortCode") String shortCode);

    @Query("SELECT c.deviceType, COUNT(c) FROM ClickLog c WHERE c.shortCode = :shortCode AND c.deviceType IS NOT NULL GROUP BY c.deviceType")
    List<Object[]> countByShortCodeGroupByDeviceType(@Param("shortCode") String shortCode);

    @Query("SELECT c.browser, COUNT(c) FROM ClickLog c WHERE c.shortCode = :shortCode AND c.browser IS NOT NULL GROUP BY c.browser ORDER BY COUNT(c) DESC")
    List<Object[]> countByShortCodeGroupByBrowser(@Param("shortCode") String shortCode);

    @Query(value = "SELECT EXTRACT(HOUR FROM timestamp) as hour, COUNT(*) as count " +
            "FROM click_logs WHERE short_code = :shortCode AND DATE(timestamp) = DATE(:date) " +
            "GROUP BY EXTRACT(HOUR FROM timestamp) ORDER BY hour", nativeQuery = true)
    List<Object[]> getHourlyClicksForDate(@Param("shortCode") String shortCode, @Param("date") Instant date);
}