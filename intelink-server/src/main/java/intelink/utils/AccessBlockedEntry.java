package intelink.utils;

import intelink.models.enums.AccessControlType;

public record AccessBlockedEntry(
        AccessControlType type,
        String value
) {
}
