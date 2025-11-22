package intelink.modules.redirect.services;

import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.ClickStatus;
import intelink.modules.redirect.repositories.DimensionStatRepository;
import intelink.utils.helper.DimensionEntry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class DimensionStatService {

    private final DimensionStatRepository dimensionStatRepository;

    @Transactional
    public void recordDimensionStats(ShortUrl shortUrl, List<DimensionEntry> dimensionEntries, ClickStatus status) {
        dimensionEntries.forEach(entry -> {
            if (entry.value() == null || entry.value().isBlank()) {
                log.warn("[ClickLogService.recordDimensionStats] Skipping empty dimension value for ShortUrl ID {}: {}", shortUrl.getId(), entry.type());
                return;
            }
            DimensionStat dimensionStat = dimensionStatRepository
                    .findByShortUrlAndTypeAndValue(shortUrl, entry.type(), entry.value())
                    .orElseGet(() -> {
                        DimensionStat d = DimensionStat.builder().shortUrl(shortUrl).type(entry.type()).value(entry.value()).build();
                        return dimensionStatRepository.save(d);
                    });
            if (status == ClickStatus.ALLOWED) {
                dimensionStatRepository.increaseAllowedCounters(dimensionStat.getId());
            } else if (status == ClickStatus.BLOCKED) {
                dimensionStatRepository.increaseBlockedCounters(dimensionStat.getId());
            }
        });
    }

}
