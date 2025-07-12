package intelink.services;

import intelink.models.ClickLog;
import intelink.models.enums.IpVersion;
import intelink.repositories.ClickLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClickLogService {

    private final ClickLogRepository clickLogRepository;

    @Transactional
    public ClickLog recordClick(String shortCode, String ipAddress, IpVersion ipVersion,
                                String normalizedIp, String subnet, String userAgent,
                                String referrer, String country, String city,
                                String browser, String os, String deviceType) {

        ClickLog clickLog = ClickLog.builder()
                .shortCode(shortCode)
                .ipAddress(ipAddress)
                .ipVersion(ipVersion)
                .normalizedIp(normalizedIp)
                .subnet(subnet)
                .userAgent(userAgent)
                .referrer(referrer)
                .country(country)
                .city(city)
                .browser(browser)
                .os(os)
                .deviceType(deviceType)
                .build();

        ClickLog saved = clickLogRepository.save(clickLog);
        log.debug("Recorded click for short code: {} from IP: {}", shortCode, ipAddress);
        return saved;
    }

    @Transactional(readOnly = true)
    public Page<ClickLog> getClicksByShortCode(String shortCode, Pageable pageable) {
        return clickLogRepository.findByShortCodeOrderByTimestampDesc(shortCode, pageable);
    }

    @Transactional(readOnly = true)
    public List<ClickLog> getClicksInTimeRange(String shortCode, Instant startTime, Instant endTime) {
        return clickLogRepository.findByShortCodeAndTimestampBetween(shortCode, startTime, endTime);
    }

    @Transactional(readOnly = true)
    public long countClicksInTimeRange(String shortCode, Instant startTime, Instant endTime) {
        return clickLogRepository.countByShortCodeAndTimestampBetween(shortCode, startTime, endTime);
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getCountryStatistics(String shortCode) {
        List<Object[]> results = clickLogRepository.countByShortCodeGroupByCountry(shortCode);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getDeviceTypeStatistics(String shortCode) {
        List<Object[]> results = clickLogRepository.countByShortCodeGroupByDeviceType(shortCode);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getBrowserStatistics(String shortCode) {
        List<Object[]> results = clickLogRepository.countByShortCodeGroupByBrowser(shortCode);
        return results.stream()
                .collect(Collectors.toMap(
                        result -> (String) result[0],
                        result -> (Long) result[1]
                ));
    }

    @Transactional(readOnly = true)
    public int[] getHourlyClicksForDate(String shortCode, LocalDate date) {
        Instant startOfDay = date.atStartOfDay().toInstant(ZoneOffset.UTC);
        List<Object[]> results = clickLogRepository.getHourlyClicksForDate(shortCode, startOfDay);

        int[] hourlyClicks = new int[24];
        for (Object[] result : results) {
            int hour = ((Number) result[0]).intValue();
            long count = ((Number) result[1]).longValue();
            if (hour >= 0 && hour < 24) {
                hourlyClicks[hour] = (int) count;
            }
        }

        return hourlyClicks;
    }

    @Transactional(readOnly = true)
    public long getTotalClicksForShortCode(String shortCode) {
        return clickLogRepository.countByShortCode(shortCode);
    }

    @Transactional(readOnly = true)
    public List<ClickLog> getRecentClicks(String shortCode, int limit) {
        Pageable pageable = Pageable.ofSize(limit);
        return clickLogRepository.findByShortCodeOrderByTimestampDesc(shortCode, pageable).getContent();
    }
}