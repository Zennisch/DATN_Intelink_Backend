package intelink.helper;

public record Cipher(
        String text,
        byte[] tweak
) {
}
