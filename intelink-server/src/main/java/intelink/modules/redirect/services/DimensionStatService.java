package intelink.modules.redirect.services;

import intelink.models.DimensionStat;
import intelink.models.ShortUrl;
import intelink.models.enums.DimensionType;
import intelink.modules.redirect.repositories.DimensionStatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class DimensionStatService {

    private final DimensionStatRepository dimensionStatRepository;

    public DimensionStat save(DimensionStat dimensionStat) {
        return dimensionStatRepository.save(dimensionStat);
    }

    public Optional<DimensionStat> findByShortUrlAndTypeAndValue(ShortUrl shortUrl, DimensionType type, String value) {
        return dimensionStatRepository.findByShortUrlAndTypeAndValue(shortUrl, type, value);
    }

}
