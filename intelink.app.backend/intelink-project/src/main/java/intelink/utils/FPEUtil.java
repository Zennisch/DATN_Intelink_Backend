package intelink.utils;

import com.privacylogistics.FF3Cipher;
import intelink.dto.Cipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.SecureRandom;

public class FPEUtil {

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final byte[] KEY = "_A_VERY_SECURED_KEY_FOR_AES_256_".getBytes();

    public static Cipher generate(Integer number, Integer length) throws IllegalBlockSizeException, BadPaddingException {
        String format = "%" + length + "s";
        String plainText = String.format(format, number).replace(' ', '0');
        byte[] tweak = randomBytes(8);

        FF3Cipher CIPHER = new FF3Cipher(KEY, tweak, ALPHABET);
        String cypherText = CIPHER.encrypt(plainText);
        return new Cipher(cypherText, tweak);
    }

    public static Integer resolve(String cypherText, byte[] tweak) throws IllegalBlockSizeException, BadPaddingException {
        FF3Cipher CIPHER = new FF3Cipher(KEY, tweak, ALPHABET);
        String plainText = CIPHER.decrypt(cypherText);
        return Integer.parseInt(plainText.replaceFirst("^0+", ""));
    }

    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

}
