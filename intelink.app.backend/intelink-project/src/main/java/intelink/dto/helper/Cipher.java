package intelink.dto.helper;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Cipher {
    private String text;
    private byte[] tweak;
}
