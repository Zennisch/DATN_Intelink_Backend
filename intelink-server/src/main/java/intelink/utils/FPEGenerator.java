package intelink.utils;

import com.privacylogistics.FF3Cipher;
import intelink.utils.helper.Cipher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.SecureRandom;

@Component
public class FPEGenerator {

    private static final SecureRandom secureRandom = new SecureRandom();
    @Value("${app.fpe.alphabet}")
    private String alphabet;
    @Value("${app.fpe.key}")
    private String fpeKey;

    public Cipher generate(Long number, Integer length) throws IllegalBlockSizeException, BadPaddingException {
        String format = "%" + length + "s";
        String plainText = String.format(format, number).replace(' ', '0');
        byte[] tweak = generateTweak(8);

        byte[] keyBytes = fpeKey.getBytes();
        FF3Cipher cipher = new FF3Cipher(keyBytes, tweak, alphabet);
        String cypherText = cipher.encrypt(plainText);
        return new Cipher(cypherText, tweak);
    }

    public Long resolve(String cypherText, byte[] tweak) throws IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = fpeKey.getBytes();
        FF3Cipher cipher = new FF3Cipher(keyBytes, tweak, alphabet);
        String plainText = cipher.decrypt(cypherText);
        return Long.parseLong(plainText.replaceFirst("^0+", ""));
    }

    private static byte[] generateTweak(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

}
