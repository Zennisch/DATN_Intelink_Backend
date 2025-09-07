package intelink.services;

import intelink.models.ShortUrlAnalysisResult;
import intelink.repositories.ShortUrlAnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisResultService {
    private final ShortUrlAnalysisResultRepository shortUrlAnalysisResultRepository;

    @Transactional
    public ShortUrlAnalysisResult save(ShortUrlAnalysisResult result) {
        return shortUrlAnalysisResultRepository.save(result);
    }

    @Transactional
    public List<ShortUrlAnalysisResult> saveAll(List<ShortUrlAnalysisResult> results) {
        return shortUrlAnalysisResultRepository.saveAll(results);
    }
}
