package intelink.configs.securities;

public record JwtErrorResponse(int status, String error, String message, String path, String timestamp) {
}
