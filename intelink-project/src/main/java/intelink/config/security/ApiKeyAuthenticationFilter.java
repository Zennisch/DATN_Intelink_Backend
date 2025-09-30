package intelink.config.security;

import intelink.models.ApiKey;
import intelink.models.User;
import intelink.services.ApiKeyService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class ApiKeyAuthenticationFilter extends OncePerRequestFilter {

    private final UserDetailsService userDetailsService;
    private final ApiKeyService apiKeyService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String apiKey = resolveApiKey(request);

        if (StringUtils.hasText(apiKey) && SecurityContextHolder.getContext().getAuthentication() == null) {
            ApiKey key = apiKeyService.validateAndGetApiKey(apiKey);
            if (key != null && key.isUsable()) {
                User user = key.getUser();
                log.info("Authenticated via API key for user: {}", user.getUsername());
                UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(user, null, userDetails.getAuthorities());
                SecurityContextHolder.getContext().setAuthentication(authentication);
                apiKeyService.saveLastUsed(key);

            }
        }

        filterChain.doFilter(request, response);
    }

    private String resolveApiKey(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("ApiKey ")) {
            return header.substring(7);
        }
        String altHeader = request.getHeader("X-API-KEY");
        if (StringUtils.hasText(altHeader)) {
            return altHeader;
        }
        return null;
    }
}
