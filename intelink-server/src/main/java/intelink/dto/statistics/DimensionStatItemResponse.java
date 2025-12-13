package intelink.dto.statistics;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DimensionStatItemResponse {
    private String name;
    private Long clicks;
    private Double percentage;
}
