package intelink.utils.helper;

import intelink.models.enums.RedirectResultType;

public record RedirectResult(
        RedirectResultType type,
        String message,
        String shortCode,
        String redirectUrl
) {
    public static RedirectResult success(String redirectUrl) {
        return new RedirectResult(RedirectResultType.SUCCESS, null, null, redirectUrl);
    }

    public static RedirectResult notFound(String shortCode) {
        return new RedirectResult(RedirectResultType.NOT_FOUND, "Short URL not found: " + shortCode, shortCode, null);
    }

    public static RedirectResult unavailable(String shortCode) {
        return new RedirectResult(RedirectResultType.UNAVAILABLE, "URL is no longer accessible: " + shortCode, shortCode, null);
    }

    public static RedirectResult accessDenied(String shortCode) {
        return new RedirectResult(RedirectResultType.ACCESS_DENIED, "Access denied for short URL: " + shortCode, shortCode, null);
    }

    public static RedirectResult passwordRequired(String unlockUrl, String shortCode) {
        return new RedirectResult(RedirectResultType.PASSWORD_PROTECTED, null, shortCode, unlockUrl);
    }

    public static RedirectResult incorrectPassword(String shortCode) {
        return new RedirectResult(RedirectResultType.INCORRECT_PASSWORD, "Incorrect password for short URL: " + shortCode, shortCode, null);
    }
}
