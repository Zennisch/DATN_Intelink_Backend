package intelink.models.news;

import intelink.models.news.enums.CustomDomainStatus;
import intelink.models.news.enums.CustomDomainVerificationMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "custom_domains")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class CustomDomain {

    // Key group
    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    // Configuration group
    @Column(name = "domain", nullable = false, unique = true, length = 255)
    private String domain;

    @Column(name = "subdomain", nullable = true, length = 100)
    private String subdomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private CustomDomainStatus status = CustomDomainStatus.PENDING_VERIFICATION;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", nullable = false)
    @Builder.Default
    private CustomDomainVerificationMethod verificationMethod = CustomDomainVerificationMethod.TXT_RECORD;

    @Column(name = "ssl_enabled", nullable = false)
    @Builder.Default
    private Boolean sslEnabled = false;

    @Column(name = "verification_token", nullable = false)
    private String verificationToken;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    // Audit group
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    // Lifecycle hooks
    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public Boolean isVerified() {
        return this.status == CustomDomainStatus.VERIFIED;
    }

    public void markAsVerified() {
        this.status = CustomDomainStatus.VERIFIED;
    }

    public void markAsFailedVerification() {
        this.status = CustomDomainStatus.FAILED_VERIFICATION;
    }
}
