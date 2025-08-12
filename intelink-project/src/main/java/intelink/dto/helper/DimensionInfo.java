package intelink.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DimensionInfo {
    String country;
    String city;
    String browser;
    String os;
    String deviceType;
}
