package intelink.dto.helper;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HourlyClick {
    private Integer hour;
    private Long totalClicks;
}
