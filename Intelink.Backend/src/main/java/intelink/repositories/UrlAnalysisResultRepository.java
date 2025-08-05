package intelink.repositories;

import intelink.models.UrlAnalysisResult;
import intelink.models.enums.AnalysisStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UrlAnalysisResultRepository extends JpaRepository<UrlAnalysisResult, UUID> {

    Optional<UrlAnalysisResult> findByShortCode(String shortCode);

    List<UrlAnalysisResult> findByStatus(AnalysisStatus status);

    @Query("SELECT u FROM UrlAnalysisResult u WHERE u.status = :status AND u.analyzedAt >= :since")
    List<UrlAnalysisResult> findByStatusAndAnalyzedAtAfter(@Param("status") AnalysisStatus status, @Param("since") Instant since);

    @Query("SELECT COUNT(u) FROM UrlAnalysisResult u WHERE u.status = :status")
    long countByStatus(@Param("status") AnalysisStatus status);

    @Query("SELECT u.status, COUNT(u) FROM UrlAnalysisResult u GROUP BY u.status")
    List<Object[]> getStatusStatistics();
}