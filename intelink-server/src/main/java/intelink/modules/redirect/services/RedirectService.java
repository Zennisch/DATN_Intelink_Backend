package intelink.modules.redirect.services;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.enums.AccessControlMode;
import intelink.models.enums.AccessControlType;
import intelink.models.enums.ClickStatus;
import intelink.modules.url.services.ShortUrlAccessControlService;
import intelink.modules.url.services.ShortUrlService;
import intelink.utils.AccessBlockedEntry;
import intelink.utils.AccessControlValidationUtil;
import intelink.utils.GeoLiteUtil;
import intelink.utils.IpUtil;
import intelink.utils.helper.IpInfo;
import intelink.utils.helper.RedirectResult;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedirectService {

    private final ShortUrlService shortUrlService;
    private final ShortUrlAccessControlService shortUrlAccessControlService;
    private final PasswordEncoder passwordEncoder;
    private final ClickLogService clickLogService;

    @Value("${app.url.template.password-unlock}")
    private String passwordUnlockUrlTemplate;

    private Boolean isShortUrlAccessible(ShortUrl shortUrl) {
        if (shortUrl.getDeletedAt() != null) {
            return false;
        }

        if (shortUrl.getEnabled() == false) {
            return false;
        }

        if (shortUrl.getExpiresAt() != null && Instant.now().isAfter(shortUrl.getExpiresAt())) {
            return false;
        }

        if (shortUrl.getMaxUsage() != null && shortUrl.getTotalClicks() >= shortUrl.getMaxUsage()) {
            return false;
        }

        return true;
    }

    @Transactional
    @RateLimiter(name = "redirect", fallbackMethod = "handleRateLimitExceeded")
    public RedirectResult handleRedirect(String shortCode, String password, HttpServletRequest request) {
        // 1. Find short URL by code
        Optional<ShortUrl> shortUrlOpt = shortUrlService.findByShortCode(shortCode);
        if (shortUrlOpt.isEmpty()) {
            log.warn("[RedirectService.handleRedirect] Short URL not found: {}", shortCode);
            return RedirectResult.notFound(shortCode);
        }

        // 2. Check if URL is accessible
        ShortUrl shortUrl = shortUrlOpt.get();
        if (!isShortUrlAccessible(shortUrl)) {
            log.warn("[RedirectService.handleRedirect] Short URL is not accessible: {}", shortCode);
            return RedirectResult.unavailable(shortCode);
        }

        // 3. Process IP information
        AccessControlMode accessControlMode = shortUrl.getAccessControlMode();
        IpInfo ipInfo = IpUtil.process(request);
        String ipAddress = ipInfo.ipAddress();
        String countryCode = GeoLiteUtil.getCountryIsoFromIp(ipAddress);
        List<AccessBlockedEntry> accessBlockedEntries = new ArrayList<>();

        // 3.1 CIDR access control check
        List<ShortUrlAccessControl> cidrAccessControls = shortUrlAccessControlService.findAllByShortUrlAndType(shortUrl, AccessControlType.CIDR);
        if (!cidrAccessControls.isEmpty()) {
            boolean ipMatchedInList = cidrAccessControls.stream().anyMatch(ac -> AccessControlValidationUtil.isIpInCidrRange(ipAddress, ac.getValue()));

            // WHITELIST: IP must be in the CIDR list
            if (accessControlMode == AccessControlMode.WHITELIST && !ipMatchedInList) {
                accessBlockedEntries.add(new AccessBlockedEntry(AccessControlType.CIDR, ipAddress));
            }

            // BLACKLIST: IP must not be in the CIDR list
            if (accessControlMode == AccessControlMode.BLACKLIST && ipMatchedInList) {
                accessBlockedEntries.add(new AccessBlockedEntry(AccessControlType.CIDR, ipAddress));
            }
        }

        // 3.2 Geography access control check
        List<ShortUrlAccessControl> geographyAccessControls = shortUrlAccessControlService.findAllByShortUrlAndType(shortUrl, AccessControlType.GEOGRAPHY);
        if (!geographyAccessControls.isEmpty()) {
            boolean countryMatchedInList = geographyAccessControls.stream().anyMatch(ac -> ac.getValue().equalsIgnoreCase(countryCode));

            // WHITELIST: Country must be in the list
            if (accessControlMode == AccessControlMode.WHITELIST && !countryMatchedInList) {
                accessBlockedEntries.add(new AccessBlockedEntry(AccessControlType.GEOGRAPHY, countryCode));
            }

            // BLACKLIST: Country must not be in the list
            if (accessControlMode == AccessControlMode.BLACKLIST && countryMatchedInList) {
                accessBlockedEntries.add(new AccessBlockedEntry(AccessControlType.GEOGRAPHY, countryCode));
            }
        }

        // 3.3. Final access decision
        if (!accessBlockedEntries.isEmpty()) {
            StringBuilder reasonBuilder = new StringBuilder();
            reasonBuilder.append(accessControlMode.name()).append("|");
            accessBlockedEntries.forEach(entry -> reasonBuilder.append(entry.type().name()).append(":").append(entry.value()).append(";"));
            String reason = reasonBuilder.toString();

            log.warn("[RedirectService.handleRedirect] Access denied for Short URL: {}. Reasons: {}", shortCode, reason);
            clickLogService.recordClick(shortUrl, request, ClickStatus.BLOCKED, reason);
            return RedirectResult.accessDenied(shortCode);
        }

        // 4. Check if URL has password and password is correct
        Optional<ShortUrlAccessControl> passwordAccessControl = shortUrlAccessControlService.findByShortUrlAndType(shortUrl, AccessControlType.PASSWORD_PROTECTED);
        if (passwordAccessControl.isPresent()) {
            if (password == null || password.isEmpty()) {
                log.info("[RedirectService.handleRedirect] Password required for Short URL: {}", shortCode);
                return RedirectResult.passwordRequired(passwordUnlockUrlTemplate.replace("{shortCode}", shortCode), shortCode);
            }
            if (!passwordEncoder.matches(password, passwordAccessControl.get().getValue())) {
                log.warn("[RedirectService.handleRedirect] Incorrect password for Short URL: {}", shortCode);
                return RedirectResult.incorrectPassword(shortCode);
            }
        }

        // 5. Record click log and redirect to original URL
        clickLogService.recordClick(shortUrl, request, ClickStatus.ALLOWED, null);
        return RedirectResult.success(shortUrl.getOriginalUrl());
    }

}
