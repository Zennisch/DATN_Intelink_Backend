// java
package intelink.dto.response.stat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AggregateByCountryResponse {
    private Integer limit;
    private String from;
    private String to;
    private List<CountryStat> data;
}