package intelink.utils;

import intelink.dto.Cipher;
import org.junit.jupiter.api.Test;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

public class FPEUtilTest {

    @Test
    public void testGenerateAndResolve() throws IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = FPEUtil.generate(123456L, 10);
        System.out.println("Generated value: " + cipher.getText());

        Long resolvedValue = FPEUtil.resolve(cipher.getText(), cipher.getTweak());
        System.out.println("Resolved value: " + resolvedValue);

        assert resolvedValue == 123456L;
    }

}
