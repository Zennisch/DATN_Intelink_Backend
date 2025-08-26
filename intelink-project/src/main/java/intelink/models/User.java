package intelink.models;

import intelink.models.enums.OAuthProvider;
import intelink.models.enums.UserRole;
import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;

@Entity
@Table(name = "users", indexes = {
        @Index(name = "idx_users_username", columnList = "username", unique = true),
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        @Index(name = "idx_users_role", columnList = "role")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class User {

    @Id
    @Column(name = "id", nullable = false, unique = true)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 16)
    private String username;

    @Column(name = "email", nullable = false, unique = true, length = 128)
    private String email;

    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 8)
    private UserRole role;

    @Builder.Default
    @Column(name = "total_clicks", nullable = false)
    private Long totalClicks = 0L;

    @Builder.Default
    @Column(name = "total_short_urls", nullable = false)
    private Integer totalShortUrls = 0;

    @Builder.Default
    @Column(name = "email_verified", nullable = false)
    private Boolean emailVerified = false;

    @Column(name = "premium_expires_at", nullable = true)
    private Instant premiumExpiresAt;

    @Builder.Default
    @Column(name = "max_short_urls", nullable = false)
    private Integer maxShortUrls = 10; // Default limit for free users

    @Builder.Default
    @Column(name = "custom_domain_enabled", nullable = false)
    private Boolean customDomainEnabled = false;

    @Builder.Default
    @Column(name = "analytics_enabled", nullable = false)
    private Boolean analyticsEnabled = false;

    @Builder.Default
    @Column(name = "api_access_enabled", nullable = false)
    private Boolean apiAccessEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false)
    @Builder.Default
    private OAuthProvider authProvider = OAuthProvider.LOCAL;

    @Column(name = "provider_user_id", nullable = true)
    private String providerUserId;

    @Column(name = "last_login_at", nullable = true)
    private Instant lastLoginAt;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    @PrePersist
    private void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    private void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public boolean isPremiumActive() {
        return role == UserRole.PREMIUM && 
               premiumExpiresAt != null && 
               Instant.now().isBefore(premiumExpiresAt);
    }

    public boolean isPremiumExpired() {
        return role == UserRole.PREMIUM && 
               (premiumExpiresAt == null || Instant.now().isAfter(premiumExpiresAt));
    }

}
