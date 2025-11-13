// java
package intelink.dto.response.stat;

import java.util.List;

public record AggregateByDimensionResponse(
        String dimension,
        Integer limit,
        String from,
        String to,
        List<DimensionStatDto> stats
) { }