package intelink.utils.helper;

import intelink.models.enums.RedirectType;

public record RedirectResult(
    RedirectType type,
    String message,
    String shortCode,
    String redirectUrl
) {
    public static RedirectResult success(String redirectUrl) {
        return new RedirectResult(RedirectType.SUCCESS, null, null, redirectUrl);
    }

    public static RedirectResult notFound(String shortCode) {
        return new RedirectResult(RedirectType.NOT_FOUND, "Short URL not found: " + shortCode, shortCode, null);
    }

    public static RedirectResult unavailable(String shortCode) {
        return new RedirectResult(RedirectType.UNAVAILABLE, "URL is no longer accessible: " + shortCode, shortCode, null);
    }

    public static RedirectResult accessDenied(String shortCode) {
        return new RedirectResult(RedirectType.ACCESS_DENIED, "Access denied for short URL: " + shortCode, shortCode, null);
    }

    public static RedirectResult passwordRequired(String unlockUrl, String shortCode) {
        return new RedirectResult(RedirectType.PASSWORD_PROTECTED, null, shortCode, unlockUrl);
    }

    public static RedirectResult incorrectPassword(String shortCode) {
        return new RedirectResult(RedirectType.INCORRECT_PASSWORD, "Incorrect password for short URL: " + shortCode, shortCode, null);
    }
}
