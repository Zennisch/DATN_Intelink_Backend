package intelink.models;

import intelink.models.enums.DimensionType;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder

@Entity
@Table(name = "dimension_stats")
public class DimensionStat {

    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    public UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    public ShortUrl shortUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    public DimensionType type;

    @Column(name = "value", nullable = false, length = 512)
    public String value;

    @Column(name = "total_clicks", nullable = false)
    @Builder.Default
    public Long totalClicks = 0L;

    @Column(name = "allowed_clicks", nullable = false)
    @Builder.Default
    public Long allowedClicks = 0L;

    @Column(name = "blocked_clicks", nullable = false)
    @Builder.Default
    public Long blockedClicks = 0L;
}
