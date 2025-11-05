package intelink.models;

import intelink.models.enums.AccessControlType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString
@Builder
public class ShortUrlAccessControl {
    public Long id;
    public ShortUrl shortUrl;
    
    public AccessControlType type;
    public String value;
}
