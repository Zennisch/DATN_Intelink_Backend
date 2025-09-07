package intelink.utils.seeder;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAnalysisResult;
import intelink.models.enums.ShortUrlAnalysisEngine;
import intelink.models.enums.ShortUrlAnalysisStatus;
import intelink.models.enums.ShortUrlAnalysisThreatType;
import intelink.models.enums.ShortUrlAnalysisPlatformType;
import intelink.repositories.ShortUrlAnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class AnalysisDataSeeder {

    private final ShortUrlAnalysisResultRepository shortUrlAnalysisResultRepository;
    private final DataSeedingUtils utils;

    public void createAnalysisResults(List<ShortUrl> shortUrls, int count) {
        log.info("Creating {} analysis results...", count);
        List<ShortUrlAnalysisResult> results = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            ShortUrl randomShortUrl = utils.getRandomElement(shortUrls);
            ShortUrlAnalysisStatus status = utils.getRandomAnalysisStatus();
            ShortUrlAnalysisEngine engine = utils.getRandom().nextDouble() < 0.8 ? 
                ShortUrlAnalysisEngine.GOOGLE_SAFE_BROWSING : 
                ShortUrlAnalysisEngine.VIRUSTOTAL;
            
            ShortUrlAnalysisThreatType threatType = status == ShortUrlAnalysisStatus.SAFE ? 
                ShortUrlAnalysisThreatType.NONE : utils.getRandomThreatType();
            
            ShortUrlAnalysisPlatformType platformType = utils.getRandomPlatformType();

            ShortUrlAnalysisResult result = ShortUrlAnalysisResult.builder()
                    .status(status)
                    .engine(engine)
                    .threatType(threatType.name())
                    .platformType(platformType.name())
                    .cacheDuration(utils.getRandom().nextDouble() < 0.5 ? "3600s" : null)
                    .details(status != ShortUrlAnalysisStatus.SAFE ? "Threat detected: " + threatType.name() : null)
                    .createdAt(utils.getRandomInstantBetween(2023, 2024))
                    .shortUrl(randomShortUrl)
                    .build();

            results.add(result);
        }

        shortUrlAnalysisResultRepository.saveAll(results);
    }
}
