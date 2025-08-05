package intelink.models.enums;

public enum TokenType {
    EMAIL_VERIFICATION, PASSWORD_RESET, OAUTH_STATE;

    public static TokenType fromString(String type) {
        try {
            return TokenType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid token type: " + type);
        }
    }
}