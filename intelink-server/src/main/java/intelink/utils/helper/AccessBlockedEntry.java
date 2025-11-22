package intelink.utils.helper;

import intelink.models.enums.AccessControlType;

public record AccessBlockedEntry(
        AccessControlType type,
        String value
) {
}
