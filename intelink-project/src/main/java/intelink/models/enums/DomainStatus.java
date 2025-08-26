package intelink.models.enums;

public enum DomainStatus {
    PENDING_VERIFICATION, VERIFIED, FAILED_VERIFICATION, SUSPENDED, DELETED;

    public static DomainStatus fromString(String status) {
        try {
            return DomainStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid domain status: " + status);
        }
    }
}
