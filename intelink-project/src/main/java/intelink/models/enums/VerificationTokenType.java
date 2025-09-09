package intelink.models.enums;

public enum VerificationTokenType {
    EMAIL_VERIFICATION, PASSWORD_RESET, OAUTH_STATE;

    public static VerificationTokenType fromString(String type) {
        try {
            return VerificationTokenType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid token type: " + type);
        }
    }
}
