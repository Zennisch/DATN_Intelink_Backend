package intelink.config.security;

import intelink.models.User;
import intelink.services.interfaces.IUserService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final IUserService userService;

    @Value("${app.host.frontend}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        Optional<User> userOpt = userService.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new ServletException("User not found");
        }

        User user = userOpt.get();

        String token = jwtTokenProvider.generateToken(user.getUsername());

        String redirectUrl = String.format(
                "%s/auth/oauth2/callback?token=%s",
                frontendUrl, token
        );
        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}
