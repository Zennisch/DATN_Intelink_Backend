package intelink.repositories;

import intelink.models.DimensionStat;
import intelink.models.enums.DimensionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DimensionStatRepository extends JpaRepository<DimensionStat, String> {

    @Query("SELECT d FROM DimensionStat d WHERE d.shortCode = :shortCode AND d.type = :type AND d.date BETWEEN :startDate AND :endDate")
    List<DimensionStat> findByShortCodeAndTypeAndDateBetween(
            @Param("shortCode") String shortCode,
            @Param("type") DimensionType type,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);

    @Query("SELECT d.value, SUM(d.clickCount) FROM DimensionStat d WHERE d.shortCode = :shortCode AND d.type = :type GROUP BY d.value ORDER BY SUM(d.clickCount) DESC")
    List<Object[]> getTopValuesByType(@Param("shortCode") String shortCode, @Param("type") DimensionType type);

    @Query("SELECT d FROM DimensionStat d WHERE d.shortCode = :shortCode AND d.date = :date")
    List<DimensionStat> findByShortCodeAndDate(@Param("shortCode") String shortCode, @Param("date") LocalDate date);

    Optional<DimensionStat> findByShortCodeAndDateAndTypeAndValue(String shortCode, LocalDate date, DimensionType type, String value);
}