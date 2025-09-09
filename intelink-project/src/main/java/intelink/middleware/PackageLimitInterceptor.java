package intelink.middleware;

import intelink.models.User;
import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.enums.SubscriptionPlanType;
import intelink.services.UserService;
import intelink.repositories.SubscriptionRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PackageLimitInterceptor implements HandlerInterceptor {

    private final UserService userService;
    private final SubscriptionRepository subscriptionRepository;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            // Nếu chưa xác thực hoặc là anonymous, bỏ qua kiểm tra package
            return true;
        }

        User user = userService.getCurrentUser();

        // Lấy subscription hiện tại (active và chưa hết hạn)
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findActiveSubscriptionByUser(user, Instant.now());
        if (subscriptionOpt.isEmpty()) {
            // Nếu không có subscription, mặc định là FREE
            return applyFreeLimit(user, request, response);
        }

        Subscription subscription = subscriptionOpt.get();
        SubscriptionPlan plan = subscription.getSubscriptionPlan();
        SubscriptionPlanType planType = plan.getType();

        switch (planType) {
            case FREE -> {
                return applyFreeLimit(user, request, response);
            }
            case PRO -> {
                // Giới hạn PRO
                if (user.getTotalClicks() >= plan.getMaxShortUrls()) {
                    response.sendError(429, "Pro package: Click limit reached");
                    return false;
                }
                // Cho phép custom short code, analytics/statistics, không giới hạn URL
            }
            case ENTERPRISE -> {
                // Không giới hạn gì, cho phép API, analytics/statistics, custom code, unlimited clicks/urls
            }
        }
        return true;
    }

    private boolean applyFreeLimit(User user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (user.getTotalClicks() >= 499) {
            response.sendError(429, "Free package: Click limit reached");
            return false;
        }
        if (user.getShortUrls().size() >= 10) {
            response.sendError(429, "Free package: Short URL limit reached");
            return false;
        }
        if (request.getRequestURI().contains("/analytics") || request.getRequestURI().contains("/statistics")) {
            response.sendError(403, "Free package: No analytics/statistics");
            return false;
        }
        if (request.getRequestURI().contains("/shorten") && request.getParameter("customCode") != null) {
            response.sendError(403, "Free package: No custom short code");
            return false;
        }
        return true;
    }
}