package intelink.utils.helper;

public record Cipher(
        String text,
        byte[] tweak
) {
}
