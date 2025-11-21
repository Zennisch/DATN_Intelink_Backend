package intelink.utils;

import intelink.utils.helper.UserAgentInfo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class UserAgentUtil {

    // Browser
    private static final Pattern EDGE_PATTERN = Pattern.compile("Edg/([\\d.]+)");
    private static final Pattern OPERA_PATTERN = Pattern.compile("OPR/([\\d.]+)");
    private static final Pattern CHROME_PATTERN = Pattern.compile("Chrome/([\\d.]+)");
    private static final Pattern FIREFOX_PATTERN = Pattern.compile("Firefox/([\\d.]+)");
    private static final Pattern SAFARI_PATTERN = Pattern.compile("Safari/([\\d.]+)");
    private static final Pattern IE_PATTERN = Pattern.compile("MSIE ([\\d.]+)");

    // OS
    private static final Pattern WINDOWS_PATTERN = Pattern.compile("Windows NT ([\\d.]+)");
    private static final Pattern MAC_PATTERN = Pattern.compile("Mac OS X ([\\d_.]+)");
    private static final Pattern LINUX_PATTERN = Pattern.compile("Linux");
    private static final Pattern ANDROID_PATTERN = Pattern.compile("Android ([\\d.]+)");
    private static final Pattern IOS_PATTERN = Pattern.compile("OS ([\\d_]+)");

    // Device
    private static final Pattern MOBILE_PATTERN = Pattern.compile("Mobile|Android|iPhone|iPad|iPod|BlackBerry|Windows Phone");
    private static final Pattern TABLET_PATTERN = Pattern.compile("iPad|Android(?!.*Mobile)|Tablet");
    private static final Pattern BOT_PATTERN = Pattern.compile("bot|crawler|spider|scraper", Pattern.CASE_INSENSITIVE);

    public static UserAgentInfo parseUserAgent(String userAgent) {
        if (!StringUtils.hasText(userAgent)) return null;

        log.debug("UserAgentUtil.parseUserAgent - Parsing user agent: {}", userAgent);

        String browser = parseBrowser(userAgent);
        String os = parseOperatingSystem(userAgent);
        String deviceType = parseDeviceType(userAgent);

        return new UserAgentInfo(browser, os, deviceType);
    }

    private static String parseBrowser(String userAgent) {
        if (BOT_PATTERN.matcher(userAgent).find()) {
            return "Bot/Crawler";
        }

        Matcher matcher;

        matcher = EDGE_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "Microsoft Edge " + matcher.group(1);
        }

        matcher = OPERA_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "Opera " + matcher.group(1);
        }

        matcher = CHROME_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "Chrome " + matcher.group(1);
        }

        matcher = FIREFOX_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "Firefox " + matcher.group(1);
        }

        matcher = SAFARI_PATTERN.matcher(userAgent);
        if (matcher.find() && !userAgent.contains("Chrome")) {
            return "Safari " + matcher.group(1);
        }

        matcher = IE_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "Internet Explorer " + matcher.group(1);
        }

        return null;
    }

    private static String parseOperatingSystem(String userAgent) {
        Matcher matcher;

        matcher = WINDOWS_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            String version = matcher.group(1);
            return "Windows " + getWindowsVersion(version);
        }

        matcher = MAC_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            String version = matcher.group(1).replace("_", ".");
            return "macOS " + version;
        }

        matcher = ANDROID_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            return "Android " + matcher.group(1);
        }

        if (LINUX_PATTERN.matcher(userAgent).find()) {
            return "Linux";
        }

        matcher = IOS_PATTERN.matcher(userAgent);
        if (matcher.find()) {
            String version = matcher.group(1).replace("_", ".");
            return "iOS " + version;
        }

        return null;
    }

    private static String getWindowsVersion(String ntVersion) {
        return switch (ntVersion) {
            case "10.0" -> "10/11";
            case "6.3" -> "8.1";
            case "6.2" -> "8";
            case "6.1" -> "7";
            case "6.0" -> "Vista";
            case "5.1" -> "XP";
            case "5.0" -> "2000";
            default -> ntVersion;
        };
    }

    private static String parseDeviceType(String userAgent) {
        if (MOBILE_PATTERN.matcher(userAgent).find()) {
            return "Mobile";
        }

        if (TABLET_PATTERN.matcher(userAgent).find()) {
            return "Tablet";
        }

        if (BOT_PATTERN.matcher(userAgent).find()) {
            return "Bot";
        }

        return "Desktop";
    }
}
