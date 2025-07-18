package intelink.models;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "monthly_stats")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class MonthlyStat {

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
    public List<DailyStat> getDailyStats() {
        List<DailyStat> dailyStats = shortUrl.getDailyStats();
        if (dailyStats == null || dailyStats.isEmpty()) {
            return new ArrayList<>();
        }

        YearMonth yearMonth = YearMonth.from(timestamp.atZone(ZoneId.systemDefault()).toLocalDate());

        return dailyStats.stream()
                .filter(dailyStat -> {
                    LocalDate dailyDate = dailyStat.getTimestamp().atZone(ZoneId.systemDefault()).toLocalDate();
                    YearMonth dailyYearMonth = YearMonth.from(dailyDate);
                    return dailyYearMonth.equals(yearMonth);
                })
                .toList();
    }

    @Transient
    public Long getTotalClicks() {
        return getDailyStats().stream()
                .mapToLong(DailyStat::getTotalClicks)
                .sum();
    }

    @Transient
    public Long getClicksForDay(int day) {
        if (day < 1 || day > 31) {
            throw new IllegalArgumentException("Day must be between 1 and 31");
        }

        return getDailyStats().stream()
                .filter(ds -> ds.getTimestamp().atZone(ZoneId.systemDefault()).getDayOfMonth() == day)
                .findFirst()
                .map(DailyStat::getTotalClicks)
                .orElse(0L);
    }
}