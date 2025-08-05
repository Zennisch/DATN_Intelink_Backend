package intelink.services;

import intelink.models.AnalysisResult;
import intelink.repositories.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class AnalysisResultService {
    private final AnalysisResultRepository analysisResultRepository;

    @Transactional
    public AnalysisResult save(AnalysisResult result) {
        return analysisResultRepository.save(result);
    }

    @Transactional
    public List<AnalysisResult> saveAll(List<AnalysisResult> results) {
        return analysisResultRepository.saveAll(results);
    }
}
