package intelink.models;

import intelink.models.enums.IpVersion;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "click_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ClickLog {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "ip_version", nullable = false)
    private IpVersion ipVersion;

    @Column(name = "ip_address", nullable = false)
    private String ipAddress;

    @Column(name = "ip_normalized", nullable = false)
    private String ipNormalized;

    @Column(name = "subnet", nullable = false)
    private String subnet;

    @Column(name = "user_agent", nullable = true, columnDefinition = "TEXT")
    private String userAgent;

    @Column(name = "referrer", nullable = true, columnDefinition = "TEXT")
    private String referrer;

    @Column(name = "country", nullable = true)
    private String country;

    @Column(name = "city", nullable = true)
    private String city;

    @Column(name = "browser", nullable = true)
    private String browser;

    @Column(name = "os", nullable = true)
    private String os;

    @Column(name = "device_type", nullable = true)
    private String deviceType;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_url_id", nullable = false)
    @ToString.Exclude
    private ShortUrl shortUrl;

    @PrePersist
    private void onCreate() {
        this.timestamp = Instant.now();
    }

}
