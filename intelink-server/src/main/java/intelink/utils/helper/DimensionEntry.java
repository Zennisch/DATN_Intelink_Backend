package intelink.utils.helper;

import intelink.models.enums.DimensionType;

public record DimensionEntry(
        DimensionType type,
        String value
) {
}
