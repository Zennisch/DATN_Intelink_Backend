package intelink.models;

import intelink.models.enums.DomainStatus;
import intelink.models.enums.VerificationMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "custom_domains", indexes = {
        @Index(name = "idx_custom_domains_domain", columnList = "domain", unique = true),
        @Index(name = "idx_custom_domains_user", columnList = "user_id"),
        @Index(name = "idx_custom_domains_status", columnList = "status"),
        @Index(name = "idx_custom_domains_verified", columnList = "verified")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class CustomDomain {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.UUID)
    @EqualsAndHashCode.Include
    private UUID id;

    @Column(name = "domain", nullable = false, unique = true, length = 255)
    private String domain;

    @Column(name = "subdomain", nullable = true, length = 100)
    private String subdomain;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private DomainStatus status = DomainStatus.PENDING_VERIFICATION;

    @Column(name = "verification_token", nullable = false)
    private String verificationToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "verification_method", nullable = false)
    @Builder.Default
    private VerificationMethod verificationMethod = VerificationMethod.TXT_RECORD;

    @Builder.Default
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Builder.Default
    @Column(name = "ssl_enabled", nullable = false)
    private Boolean sslEnabled = false;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "verified_at", nullable = true)
    private Instant verifiedAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
        if (this.verificationToken == null) {
            this.verificationToken = UUID.randomUUID().toString();
        }
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean isVerified() {
        return verified && status == DomainStatus.VERIFIED;
    }

    public void markAsVerified() {
        this.verified = true;
        this.status = DomainStatus.VERIFIED;
        this.verifiedAt = Instant.now();
    }

    public void markAsFailed() {
        this.verified = false;
        this.status = DomainStatus.FAILED_VERIFICATION;
    }
}
