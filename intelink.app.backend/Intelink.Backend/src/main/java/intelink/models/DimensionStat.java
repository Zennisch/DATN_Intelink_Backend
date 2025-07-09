package intelink.models;

import intelink.models.enums.DimensionType;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "dimension_stats", indexes = {
        @Index(name = "idx_short_code", columnList = "short_code"),
        @Index(name = "idx_date", columnList = "date"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_composite", columnList = "short_code,date,type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class DimensionStat {

    @Id
    @Column(name = "id", nullable = false, unique = true, length = 100)
    @EqualsAndHashCode.Include
    private String id;

    @Column(name = "short_code", nullable = false, length = 20)
    private String shortCode;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private DimensionType type;

    @Column(name = "value", nullable = false, length = 255)
    private String value;

    @Builder.Default
    @Column(name = "click_count", nullable = false)
    private long clickCount = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_code", referencedColumnName = "short_code", insertable = false, updatable = false)
    private ShortUrl shortUrl;

    @PrePersist
    private void onCreate() {
        if (this.id == null) {
            this.id = this.shortCode + "_" + this.date.toString() + "_" + this.type + "_" +
                     this.value.hashCode();
        }
    }
}