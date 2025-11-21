package intelink.modules.redirect;

import intelink.models.ShortUrl;
import intelink.models.ShortUrlAccessControl;
import intelink.models.enums.AccessControlMode;
import intelink.models.enums.AccessControlType;
import intelink.modules.url.ShortUrlAccessControlService;
import intelink.modules.url.ShortUrlService;
import intelink.utils.AccessControlValidationUtil;
import intelink.utils.GeoLiteUtil;
import intelink.utils.IpUtil;
import intelink.utils.helper.IpProcessResult;
import intelink.utils.helper.RedirectResult;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedirectService {

    private final ShortUrlService shortUrlService;
    private final ShortUrlAccessControlService shortUrlAccessControlService;
    private final PasswordEncoder passwordEncoder;

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
        IpProcessResult ipProcessResult = IpUtil.process(request);
        String ipAddress = ipProcessResult.ipAddress();
        String country = GeoLiteUtil.getCountryNameFromIp(ipAddress);
        String countryCode = GeoLiteUtil.getCountryIsoFromIp(ipAddress);
        AccessControlMode accessControlMode = shortUrl.getAccessControlMode();

        // 3.1 CIDR access control check
        List<ShortUrlAccessControl> cidrAccessControls = shortUrlAccessControlService.findAllByShortUrlAndType(shortUrl, AccessControlType.CIDR);
        if (!cidrAccessControls.isEmpty()) {
            boolean ipMatchedInList = cidrAccessControls.stream()
                    .anyMatch(ac -> AccessControlValidationUtil.isIpInCidrRange(ipAddress, ac.getValue()));

            // WHITELIST: IP must be in the CIDR list
            if (accessControlMode == AccessControlMode.WHITELIST && !ipMatchedInList) {
                log.warn("[RedirectService.handleRedirect] IP {} not in CIDR whitelist for Short URL: {}", ipAddress, shortCode);
                return RedirectResult.accessDenied(shortCode);
            }

            // BLACKLIST: IP must not be in the CIDR list
            if (accessControlMode == AccessControlMode.BLACKLIST && ipMatchedInList) {
                log.warn("[RedirectService.handleRedirect] IP {} blocked by CIDR blacklist for Short URL: {}", ipAddress, shortCode);
                return RedirectResult.accessDenied(shortCode);
            }
        }

        // 3.2 Geography access control check
        List<ShortUrlAccessControl> geographyAccessControls = shortUrlAccessControlService.findAllByShortUrlAndType(shortUrl, AccessControlType.GEOGRAPHY);
        if (!geographyAccessControls.isEmpty()) {
            boolean countryMatchedInList = geographyAccessControls.stream()
                    .anyMatch(ac -> ac.getValue().equalsIgnoreCase(countryCode));

            // WHITELIST: Country must be in the list
            if (accessControlMode == AccessControlMode.WHITELIST && !countryMatchedInList) {
                log.warn("[RedirectService.handleRedirect] Country {} ({}) not in whitelist for Short URL: {}", country, countryCode, shortCode);
                return RedirectResult.accessDenied(shortCode);
            }

            // BLACKLIST: Country must not be in the list
            if (accessControlMode == AccessControlMode.BLACKLIST && countryMatchedInList) {
                log.warn("[RedirectService.handleRedirect] Country {} ({}) blocked by blacklist for Short URL: {}", country, countryCode, shortCode);
                return RedirectResult.accessDenied(shortCode);
            }
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

        return RedirectResult.success(shortUrl.getOriginalUrl());
    }

}
