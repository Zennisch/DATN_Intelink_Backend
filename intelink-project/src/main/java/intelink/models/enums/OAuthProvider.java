package intelink.models.enums;

public enum OAuthProvider {
    GOOGLE, GITHUB, LOCAL;

    public static OAuthProvider fromString(String provider) {
        try {
            return OAuthProvider.valueOf(provider.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid OAuth provider: " + provider);
        }
    }
}