package intelink.utils.seeder;

import intelink.models.AnalysisResult;
import intelink.models.ShortUrl;
import intelink.models.enums.AnalysisStatus;
import intelink.repositories.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisDataSeeder {

    private final AnalysisResultRepository analysisResultRepository;
    private final DataSeedingUtils utils;

    public void createAnalysisResults(List<ShortUrl> shortUrls, int count) {
        log.info("Creating {} analysis results...", count);
        List<AnalysisResult> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ShortUrl randomShortUrl = utils.getRandomElement(shortUrls);
            AnalysisStatus status = utils.getRandomAnalysisStatus();

            AnalysisResult result = AnalysisResult.builder()
                    .status(status)
                    .analysisEngine(utils.getRandom().nextDouble() < 0.8 ? "GOOGLE_SAFE_BROWSING" : "VIRUS_TOTAL")
                    .threatType(status == AnalysisStatus.SAFE ? "NONE" : utils.getRandomThreatType())
                    .platformType(utils.getRandomPlatformType())
                    .cacheDuration(utils.getRandom().nextDouble() < 0.5 ? "3600s" : null)
                    .details(status != AnalysisStatus.SAFE ? "Threat detected: " + utils.getRandomThreatType() : null)
                    .analyzedAt(utils.getRandomInstantBetween(2023, 2024))
                    .shortUrl(randomShortUrl)
                    .build();

            results.add(result);
        }

        analysisResultRepository.saveAll(results);
    }
}
