package intelink.models;

import intelink.models.enums.ClickStatus;
import intelink.models.enums.IPVersion;
import jakarta.persistence.PrePersist;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ClickLog {
    public UUID id;
    public ShortUrl shortUrl;
    public IPVersion ipVersion;
    public String ipAddress;
    public String userAgent;
    public String referrer;
    public ClickStatus status;
    public Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = Instant.now();
        }
    }
}
