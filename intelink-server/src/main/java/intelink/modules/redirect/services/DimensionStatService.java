package intelink.modules.redirect.services;

import intelink.models.ShortUrl;
import intelink.models.enums.ClickStatus;
import intelink.modules.redirect.repositories.DimensionStatRepository;
import intelink.utils.helper.DimensionEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DimensionStatService {

    private final DimensionStatRepository dimensionStatRepository;

    @Transactional
    public void recordDimensionStats(ShortUrl shortUrl, List<DimensionEntry> dimensionEntries, ClickStatus status) {
        int isAllowed = (status == ClickStatus.ALLOWED) ? 1 : 0;
        dimensionEntries.forEach(entry -> {
            if (entry.value() == null || entry.value().isBlank()) {
                log.warn("[DimensionStatService.recordDimensionStats] Skipping empty dimension value for ShortUrl ID {}: {}", shortUrl.getId(), entry.type());
                return;
            }
            try {
                // Use atomic MERGE operation to avoid deadlock
                dimensionStatRepository.upsertAndIncrement(
                    shortUrl.getId(), 
                    entry.type().name(), 
                    entry.value(), 
                    isAllowed
                );
            } catch (Exception e) {
                log.error("[DimensionStatService.recordDimensionStats] Error upserting dimension stat for ShortUrl ID {}: {}", shortUrl.getId(), e.getMessage());
            }
        });
    }

}
