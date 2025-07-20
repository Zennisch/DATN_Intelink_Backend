package intelink.utils;

import com.privacylogistics.FF3Cipher;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.security.SecureRandom;

public class FPEUtil {

    private final FF3Cipher CIPHER;
    private static final SecureRandom secureRandom = new SecureRandom();

    public FPEUtil(byte[] key, byte[] tweak) {
        String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        this.CIPHER = new FF3Cipher(key, tweak, ALPHABET);
    }

    public String generate(Integer number, Integer length) throws IllegalBlockSizeException, BadPaddingException {
        String format = "%" + length + "s";
        String plainText = String.format(format, number).replace(' ', '0');

        return CIPHER.encrypt(plainText);
    }

    public Integer resolve(String cypherText) throws IllegalBlockSizeException, BadPaddingException {
        String plainText = CIPHER.decrypt(cypherText);
        return Integer.parseInt(plainText.replaceFirst("^0+", ""));
    }

    public static byte[] randomBytes(int length) {
        byte[] bytes = new byte[length];
        secureRandom.nextBytes(bytes);
        return bytes;
    }

}
