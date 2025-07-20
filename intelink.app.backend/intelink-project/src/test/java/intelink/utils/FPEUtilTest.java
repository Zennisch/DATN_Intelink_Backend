package intelink.utils;

import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public class FPEUtilTest {

    private final byte[] key = "_A_VERY_SECURED_KEY_FOR_AES_256_".getBytes();
    private final byte[] tweak = FPEUtil.randomBytes(8);
    private final FPEUtil fpeUtil = new FPEUtil(key, tweak);

    @Test
    public void testGenerateAndResolve() throws IllegalBlockSizeException, BadPaddingException {
        String value = fpeUtil.generate(123456, 10);
        System.out.println("Generated value: " + value);

        Integer resolvedValue = fpeUtil.resolve(value);
        System.out.println("Resolved value: " + resolvedValue);

        assert resolvedValue == 123456;
    }

}
