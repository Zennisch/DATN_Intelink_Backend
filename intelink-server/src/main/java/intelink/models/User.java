package intelink.models;

import intelink.models.enums.UserRole;
import intelink.models.enums.UserStatus;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder

@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    public Long id;

    @Size(min = 6, max = 32)
    @Column(name = "username", nullable = false, unique = true)
    public String username;

    @Email
    @Column(name = "email", nullable = false, unique = true, length = 128)
    public String email;

    @Column(name = "password", nullable = false)
    @ToString.Exclude
    public String password;

    @Column(name = "verified", nullable = false)
    @Builder.Default
    public Boolean verified = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    public UserRole role = UserRole.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    public UserStatus status = UserStatus.ACTIVE;

    @Column(name = "last_login_at")
    public Instant lastLoginAt;

    @Column(name = "profile_name", length = 64)
    public String profileName;

    @Column(name = "profile_picture_url", length = 256)
    public String profilePictureURL;

    @Column(name = "total_short_urls", nullable = false)
    @Builder.Default
    public Integer totalShortUrls = 0;

    @Column(name = "total_clicks", nullable = false)
    @Builder.Default
    public Long totalClicks = 0L;

    @Column(name = "balance", nullable = false)
    @Builder.Default
    public Double balance = 0.0;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    public String currency = "VND";

    @Column(name = "created_at", nullable = false, updatable = false)
    public Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    public Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
    }
}
