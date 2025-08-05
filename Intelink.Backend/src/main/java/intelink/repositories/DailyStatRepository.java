package intelink.repositories;

import intelink.models.DailyStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DailyStatRepository extends JpaRepository<DailyStat, String> {

    Optional<DailyStat> findByShortCodeAndDate(String shortCode, LocalDate date);

    @Query("SELECT d FROM DailyStat d WHERE d.shortCode = :shortCode AND d.date BETWEEN :startDate AND :endDate ORDER BY d.date")
    List<DailyStat> findByShortCodeAndDateBetween(
            @Param("shortCode") String shortCode,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT SUM(d.clickCount) FROM DailyStat d WHERE d.shortCode = :shortCode")
    Long getTotalClicksByShortCode(@Param("shortCode") String shortCode);

    @Query("SELECT d FROM DailyStat d WHERE d.shortCode = :shortCode ORDER BY d.date DESC")
    List<DailyStat> findByShortCodeOrderByDateDesc(@Param("shortCode") String shortCode);

    @Query("SELECT d.date, SUM(d.clickCount) FROM DailyStat d WHERE d.date BETWEEN :startDate AND :endDate GROUP BY d.date ORDER BY d.date")
    List<Object[]> getGlobalDailyStats(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}