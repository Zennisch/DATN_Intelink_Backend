package intelink.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "daily_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class DailyStat {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    @ToString.Exclude
    private ShortUrl shortUrl;

    @Transient
    public List<HourlyStat> getHourlyStats() {
        List<HourlyStat> hourlyStats = shortUrl.getHourlyStats();
        if (hourlyStats == null || hourlyStats.isEmpty()) {
            return new ArrayList<>();
        }

        LocalDate localDate = timestamp.atZone(ZoneId.systemDefault()).toLocalDate();

        return hourlyStats.stream()
                .filter(hourlyStat -> {
                    LocalDate hourlyStatDate = hourlyStat.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate();
                    return hourlyStatDate.equals(localDate);
                })
                .toList();
    }

    @Transient
    public Long getTotalClicks() {
        return getHourlyStats().stream()
                .mapToLong(HourlyStat::getTotalClicks)
                .sum();
    }

    @Transient
    public Long getClicksForHour(int hour) {
        if (hour < 0 || hour > 23) {
            throw new IllegalArgumentException("Hour must be between 0 and 23");
        }

        return getHourlyStats().stream()
                .filter(hs -> hs.getTimestamp().atZone(ZoneId.systemDefault()).getHour() == hour)
                .findFirst()
                .map(HourlyStat::getTotalClicks)
                .orElse(0L);
    }
}
