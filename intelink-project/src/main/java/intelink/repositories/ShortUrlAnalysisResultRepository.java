package intelink.repositories;

import intelink.models.ShortUrlAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShortUrlAnalysisResultRepository extends JpaRepository<ShortUrlAnalysisResult, Long> {
}
