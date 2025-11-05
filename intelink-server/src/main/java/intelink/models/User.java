package intelink.models;

import intelink.models.enums.UserRole;
import intelink.models.enums.UserStatus;
import java.time.Instant;

public class User {
    public Long id;
    
    public String username;
    public String email;
    public String password;
    
    public Boolean verified;
    public UserRole role;
    public UserStatus status;
    public Instant lastLoginAt;
    
    public String profileName;
    public String profilePictureURL;
    
    public Integer totalShortUrls;
    public Long totalClicks;
    
    public Double balance;
    public String currency;
    
    public Instant createdAt;
    public Instant updatedAt;
}
