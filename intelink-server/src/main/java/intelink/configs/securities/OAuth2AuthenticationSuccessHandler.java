package intelink.configs.securities;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final String REDIRECT_URL = "%s/auth/oauth2/callback?token=%s";
    private final JwtTokenProvider jwtTokenProvider;
    @Value("${app.host.frontend}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        if (email == null || email.isEmpty()) {
            throw new IllegalArgumentException("Email not found from OAuth2 provider");
        }
        String token = jwtTokenProvider.generateToken(email);

        String redirectUrl = String.format(REDIRECT_URL, frontendUrl, token);
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
