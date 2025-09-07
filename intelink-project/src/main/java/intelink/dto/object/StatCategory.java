package intelink.dto.object;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class StatCategory {
    private String category;
    private Long totalClicks;
    private List<StatEntry> data;
}
