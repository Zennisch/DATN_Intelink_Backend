package intelink.services;

import intelink.config.security.JwtTokenProvider;
import intelink.dto.object.Auth;
import intelink.models.OAuthAccount;
import intelink.models.User;
import intelink.models.enums.UserProvider;
import intelink.models.enums.UserRole;
import intelink.repositories.OAuthAccountRepository;
import intelink.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuthService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        UserProvider provider = UserProvider.fromString(registrationId.toUpperCase());

        return processOAuth2User(oAuth2User, provider);
    }

    private OAuth2User processOAuth2User(OAuth2User oAuth2User, UserProvider provider) {
        String providerUserId;
        if (provider == UserProvider.GOOGLE) {
            providerUserId = oAuth2User.getAttribute("sub");
        } else {
            providerUserId = oAuth2User.getAttribute("id");
        }

        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        Optional<OAuthAccount> oAuthAccountOpt = oAuthAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId);

        if (oAuthAccountOpt.isPresent()) {
            OAuthAccount oAuthAccount = oAuthAccountOpt.get();
            oAuthAccount.setProviderEmail(email);
            oAuthAccount.setProviderUsername(name);

            User user = oAuthAccount.getUser();
            if (user.getProvider() == oAuthAccount.getProvider()
                    && user.getEmail().equals(email)) {
                user.setEmail(email);
                user.setEmailVerified(true);
            }
            user.setLastLoginAt(Instant.now());
            userRepository.save(user);
            oAuthAccountRepository.save(oAuthAccount);

            return oAuth2User;
        }

        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;
        if (existingUser.isPresent()) {
            user = existingUser.get();
        } else {
            String baseUsername = name.toLowerCase().replaceAll("[^a-z0-9]", "");
            String username = baseUsername;
            int counter = 1;

            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter++;
            }

            user = userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash("")
                    .role(UserRole.USER)
                    .emailVerified(true)
                    .provider(provider)
                    .build()
            );
        }

        OAuthAccount oAuthAccount = OAuthAccount.builder()
                .user(user)
                .provider(provider)
                .providerUserId(providerUserId)
                .providerEmail(email)
                .providerUsername(name)
                .build();

        oAuthAccountRepository.save(oAuthAccount);

        return oAuth2User;
    }

    public Auth callback(String authToken) {
        String email = jwtTokenProvider.getUsernameFromToken(authToken);
        Optional<User> userOpt = userRepository.findByEmail(email);

        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        User user = userOpt.get();

        String token = jwtTokenProvider.generateToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        Long expiresAt = jwtTokenProvider.getExpirationTimeFromToken(token);

        return new Auth(user, token, refreshToken, expiresAt);
    }
}
