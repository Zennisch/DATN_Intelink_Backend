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
    @Value("${app.fpe.tweak}")
    private String fpeTweak;

    public Cipher generate(Long number, Integer length) throws IllegalBlockSizeException, BadPaddingException {
        String format = "%" + length + "s";
        String plainText = String.format(format, number).replace(' ', '0');

        byte[] keyBytes = fpeKey.getBytes();
        byte[] tweakBytes = fpeTweak.getBytes();

        FF3Cipher cipher = new FF3Cipher(keyBytes, tweakBytes, alphabet);
        String cypherText = cipher.encrypt(plainText);
        return new Cipher(cypherText, tweakBytes);
    }

    public Long resolve(String cypherText, byte[] tweakBytes) throws IllegalBlockSizeException, BadPaddingException {
        byte[] keyBytes = fpeKey.getBytes();
        FF3Cipher cipher = new FF3Cipher(keyBytes, tweakBytes, alphabet);
        String plainText = cipher.decrypt(cypherText);
        return Long.parseLong(plainText.replaceFirst("^0+", ""));
    }

}
