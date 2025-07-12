package intelink.models;

import intelink.models.enums.IpVersion;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "click_logs", indexes = {
        @Index(name = "idx_short_code_timestamp", columnList = "short_code,timestamp"),
        @Index(name = "idx_country_device", columnList = "country,device_type")
})
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

    @Column(name = "short_code", nullable = false, length = 20)
    private String shortCode;

    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;

    @Column(name = "ip_address", nullable = true, length = 45)
    private String ipAddress;

    @Enumerated(EnumType.STRING)
    @Column(name = "ip_version", length = 6)
    private IpVersion ipVersion;

    @Column(name = "normalized_ip", length = 50)
    private String normalizedIp;

    @Column(name = "user_agent", nullable = true, length = 512)
    private String userAgent;

    @Column(name = "referrer", nullable = true, length = 2048)
    private String referrer;

    @Column(name = "country", nullable = true, length = 100)
    private String country;

    @Column(name = "city", nullable = true, length = 100)
    private String city;

    @Column(name = "browser", nullable = true, length = 50)
    private String browser;

    @Column(name = "os", nullable = true, length = 50)
    private String os;

    @Column(name = "device_type", nullable = true, length = 20)
    private String deviceType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "short_code", referencedColumnName = "short_code", insertable = false, updatable = false)
    private ShortUrl shortUrl;

    @PrePersist
    private void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}