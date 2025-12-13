package intelink.dto.statistics;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DimensionStatResponse {
    private String category;
    private Long totalClicks;
    private List<DimensionStatItemResponse> data;
}
