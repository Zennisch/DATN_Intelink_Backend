package intelink.middleware;

import intelink.models.Subscription;
import intelink.models.SubscriptionPlan;
import intelink.models.User;
import intelink.models.enums.SubscriptionPlanType;
import intelink.repositories.SubscriptionRepository;
import intelink.services.interfaces.IUserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Instant;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PackageLimitInterceptor implements HandlerInterceptor {

    private final IUserService userService;
    private final SubscriptionRepository subscriptionRepository;

    private boolean applyFreeLimit(User user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        if (user.getTotalClicks() >= 499) {
            response.sendError(429, "Free package: Click limit reached");
            return false;
        }
        if (user.getShortUrls().size() >= 10) {
            response.sendError(429, "Free package: Short URL limit reached");
            return false;
        }
        if (request.getRequestURI().contains("/statistics")) {
            response.sendError(403, "Free package: No access to statistics");
            return false;
        }
        if (request.getRequestURI().contains("/url") && request.getParameter("customCode") != null) {
            response.sendError(403, "Free package: No access to custom codes");
            return false;
        }
        return true;
    }

    private boolean applyProLimit(User user, HttpServletRequest request, HttpServletResponse response) throws Exception {
        return true;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getPrincipal().equals("anonymousUser")) {
            return true;
        }

        User user = userService.getCurrentUser();

        // Get current active subscription
        Optional<Subscription> subscriptionOpt = subscriptionRepository.findActiveSubscriptionByUser(user, Instant.now());
        if (subscriptionOpt.isEmpty()) {
            // No active subscription, apply free limits
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
                return applyProLimit(user, request, response);
            }
            case ENTERPRISE -> {
            }
        }
        return true;
    }


}