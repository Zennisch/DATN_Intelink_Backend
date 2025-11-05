package intelink.models;

import intelink.models.enums.UserProvider;
import java.time.Instant;
import java.util.UUID;

public class OAuthAccount {
    public UUID id;
    public User user;
    
    public UserProvider provider;
    public String providerUserId;
    public String providerUsername;
    public String providerEmail;
    
    public String accessToken;
    public String refreshToken;
    public Instant tokenExpiresAt;
    
    public Instant createdAt;
    public Instant updatedAt;
}
