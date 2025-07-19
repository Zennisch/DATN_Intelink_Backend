package intelink.repositories;

import intelink.dto.HourlyClickDTO;
import intelink.models.ClickStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;

public interface ClickStatRepository extends JpaRepository<ClickStat, Long> {

    @Modifying
    @Query("UPDATE ClickStat cs SET cs.totalClicks = cs.totalClicks + 1 WHERE cs.shortUrl.id = :shortUrlId AND cs.timestamp = :timestamp")
    void incrementTotalClicks(@Param("shortUrlId") String shortUrlId, @Param("timestamp") Instant timestamp);

    @Query("""
                SELECT new intelink.dto.HourlyClickDTO(
                    EXTRACT(HOUR FROM cs.timestamp) AS hour,
                    SUM(cs.totalClicks) AS totalClicks
                )
                FROM ClickStat cs
                WHERE cs.shortUrl.id = :shortUrlId
                AND cs.timestamp >= :dayStart
                AND cs.timestamp < :dayEnd
                GROUP BY EXTRACT(HOUR FROM cs.timestamp)
                ORDER BY EXTRACT(HOUR FROM cs.timestamp)
            """)
    List<HourlyClickDTO> findHourlyClickStats(
            @Param("shortUrlId") String shortUrlId,
            @Param("dayStart") Instant dayStart,
            @Param("dayEnd") Instant dayEnd
    );
}
