package intelink.models.enums;

public enum CustomDomainVerificationMethod {
    TXT_RECORD, CNAME_RECORD, HTML_FILE;

    public static CustomDomainVerificationMethod fromString(String method) {
        try {
            return CustomDomainVerificationMethod.valueOf(method.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid verification method: " + method);
        }
    }
}
