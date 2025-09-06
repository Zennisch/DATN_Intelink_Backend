package intelink.models.news.enums;

public enum UserVerificationTokenType {
    EMAIL_VERIFICATION, PASSWORD_RESET, OAUTH_STATE;

    public static UserVerificationTokenType fromString(String type) {
        try {
            return UserVerificationTokenType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid token type: " + type);
        }
    }
}
