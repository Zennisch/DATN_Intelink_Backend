package intelink.models.news.enums;

public enum CustomDomainStatus {
    PENDING_VERIFICATION, VERIFIED, FAILED_VERIFICATION, SUSPENDED, DELETED;

    public static CustomDomainStatus fromString(String status) {
        try {
            return CustomDomainStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid custom domain status: " + status);
        }
    }
}
