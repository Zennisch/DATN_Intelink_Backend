package intelink.modules.url.services;

import intelink.dto.safebrowsing.SafeBrowsingResponse;
import intelink.models.ShortUrl;
import intelink.models.ShortUrlAnalysisResult;
import intelink.models.enums.ShortUrlAnalysisEngine;
import intelink.models.enums.ShortUrlAnalysisStatus;
import intelink.modules.url.repositories.ShortUrlAnalysisResultRepository;
import intelink.modules.utils.services.SafeBrowsingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShortUrlAnalysisService {

    private final ShortUrlAnalysisResultRepository analysisResultRepository;
    private final SafeBrowsingService safeBrowsingService;

    @Async("urlAnalysisExecutor")
    @Transactional
    public CompletableFuture<Void> analyzeUrl(ShortUrl shortUrl) {
        try {
            log.info("[ShortUrlAnalysisService.analyzeUrl] Starting analysis for ShortUrl ID: {}, URL: {}", 
                    shortUrl.getId(), shortUrl.getOriginalUrl());

            SafeBrowsingResponse response = safeBrowsingService.checkUrl(shortUrl.getOriginalUrl());

            if (response.getMatches() == null || response.getMatches().isEmpty()) {
                // URL is safe
                ShortUrlAnalysisResult safeResult = ShortUrlAnalysisResult.builder()
                        .shortUrl(shortUrl)
                        .status(ShortUrlAnalysisStatus.SAFE)
                        .engine(ShortUrlAnalysisEngine.GOOGLE_SAFE_BROWSING)
                        .threatType("NONE")
                        .platformType("ANY_PLATFORM")
                        .cacheDuration(null)
                        .details("No threats detected")
                        .build();
                analysisResultRepository.save(safeResult);
                log.info("[ShortUrlAnalysisService.analyzeUrl] URL is SAFE: {}", shortUrl.getOriginalUrl());
            } else {
                // Process each threat match
                for (SafeBrowsingResponse.ThreatMatch match : response.getMatches()) {
                    ShortUrlAnalysisStatus status = determineStatus(match.getThreatType());
                    
                    String details = buildDetails(match);

                    ShortUrlAnalysisResult result = ShortUrlAnalysisResult.builder()
                            .shortUrl(shortUrl)
                            .status(status)
                            .engine(ShortUrlAnalysisEngine.GOOGLE_SAFE_BROWSING)
                            .threatType(match.getThreatType())
                            .platformType(match.getPlatformType())
                            .cacheDuration(match.getCacheDuration())
                            .details(details)
                            .build();
                    analysisResultRepository.save(result);
                    log.warn("[ShortUrlAnalysisService.analyzeUrl] Threat detected: {} for URL: {}", 
                            match.getThreatType(), shortUrl.getOriginalUrl());
                }
            }

        } catch (Exception e) {
            log.error("[ShortUrlAnalysisService.analyzeUrl] Error analyzing ShortUrl ID {}: {}", 
                    shortUrl.getId(), e.getMessage(), e);
            
            // Save error result
            try {
                ShortUrlAnalysisResult errorResult = ShortUrlAnalysisResult.builder()
                        .shortUrl(shortUrl)
                        .status(ShortUrlAnalysisStatus.UNKNOWN)
                        .engine(ShortUrlAnalysisEngine.GOOGLE_SAFE_BROWSING)
                        .threatType("ERROR")
                        .platformType("ANY_PLATFORM")
                        .cacheDuration(null)
                        .details("Analysis failed: " + e.getMessage())
                        .build();
                analysisResultRepository.save(errorResult);
            } catch (Exception saveError) {
                log.error("[ShortUrlAnalysisService.analyzeUrl] Failed to save error result: {}", saveError.getMessage());
            }
        }
        return CompletableFuture.completedFuture(null);
    }

    public List<ShortUrlAnalysisResult> getAnalysisResults(ShortUrl shortUrl) {
        return analysisResultRepository.findByShortUrlOrderByCreatedAtDesc(shortUrl);
    }

    private ShortUrlAnalysisStatus determineStatus(String threatType) {
        return switch (threatType.toUpperCase()) {
            case "MALWARE", "UNWANTED_SOFTWARE", "POTENTIALLY_HARMFUL_APPLICATION" -> ShortUrlAnalysisStatus.MALWARE;
            case "SOCIAL_ENGINEERING" -> ShortUrlAnalysisStatus.SOCIAL_ENGINEERING;
            default -> ShortUrlAnalysisStatus.MALICIOUS;
        };
    }

    private String buildDetails(SafeBrowsingResponse.ThreatMatch match) {
        StringBuilder details = new StringBuilder();
        details.append("Threat Type: ").append(match.getThreatType());
        details.append(", Platform: ").append(match.getPlatformType());
        details.append(", Entry Type: ").append(match.getThreatEntryType());
        
        if (match.getThreatEntryMetadata() != null && match.getThreatEntryMetadata().getEntries() != null) {
            details.append(", Metadata: {");
            for (SafeBrowsingResponse.MetadataEntry entry : match.getThreatEntryMetadata().getEntries()) {
                details.append(entry.getKey()).append("=").append(entry.getValue()).append("; ");
            }
            details.append("}");
        }
        
        return details.toString();
    }
}
