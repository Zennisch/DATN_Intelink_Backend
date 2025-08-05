package intelink.exceptions;

public class ShortUrlUnavailableException extends RuntimeException {
    public ShortUrlUnavailableException(String message) {
        super(message);
    }
}
