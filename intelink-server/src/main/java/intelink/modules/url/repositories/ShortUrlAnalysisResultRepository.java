package intelink.modules.url.repositories;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ShortUrlAnalysisResultRepository extends JpaRepository<ShortUrlAnalysisResult, UUID> {

    List<ShortUrlAnalysisResult> findByShortUrl(ShortUrl shortUrl);

    List<ShortUrlAnalysisResult> findByShortUrlOrderByCreatedAtDesc(ShortUrl shortUrl);
}
