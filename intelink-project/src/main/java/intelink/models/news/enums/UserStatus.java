package intelink.models.news.enums;

public enum UserStatus {
    ACTIVE, INACTIVE, BANNED;

    public static UserStatus fromString(String status) {
        try {
            return UserStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user status: " + status);
        }
    }
}
