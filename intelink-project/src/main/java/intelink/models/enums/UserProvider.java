package intelink.models.enums;

public enum UserProvider {
    GOOGLE, GITHUB, LOCAL;

    public static UserProvider fromString(String provider) {
        try {
            return UserProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid user provider: " + provider);
        }
    }
}
