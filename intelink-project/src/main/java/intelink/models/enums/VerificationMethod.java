package intelink.models.enums;

public enum VerificationMethod {
    TXT_RECORD, CNAME_RECORD, HTML_FILE;

    public static VerificationMethod fromString(String method) {
        try {
            return VerificationMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid verification method: " + method);
        }
    }
}
