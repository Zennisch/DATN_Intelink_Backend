package intelink.services;

import intelink.config.security.JwtTokenProvider;
import intelink.dto.object.AuthToken;
import intelink.models.OAuthAccount;
import intelink.models.User;
import intelink.models.enums.UserProvider;
import intelink.models.enums.UserRole;
import intelink.repositories.OAuthAccountRepository;
import intelink.repositories.UserRepository;
import intelink.services.interfaces.IOAuthService;
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
public class OAuthService extends DefaultOAuth2UserService implements IOAuthService {

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
        // 1. Extract user info from OAuth2User
        String providerUserId;
        if (provider == UserProvider.GOOGLE) {
            providerUserId = oAuth2User.getAttribute("sub");
        } else {
            providerUserId = oAuth2User.getAttribute("id");
        }

        // 2. Extract email and name
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        // 3. Check if OAuthAccount exists
        Optional<OAuthAccount> oAuthAccountOpt = oAuthAccountRepository
                .findByProviderAndProviderUserId(provider, providerUserId);

        // 4. If exists, update and return
        if (oAuthAccountOpt.isPresent()) {
            OAuthAccount oAuthAccount = oAuthAccountOpt.get();
            oAuthAccount.setProviderEmail(email);
            oAuthAccount.setProviderUsername(name);

            User user = oAuthAccount.getUser();
            if (user.getProvider() == oAuthAccount.getProvider() && user.getEmail().equals(email)) {
                user.setEmail(email);
                user.setEmailVerified(true);
            }
            user.setLastLoginAt(Instant.now());

            userRepository.save(user);
            oAuthAccountRepository.save(oAuthAccount);

            return oAuth2User;
        }

        // 5. If not exists, find or create User
        Optional<User> existingUser = userRepository.findByEmail(email);
        User user;

        if (existingUser.isPresent()) {
            // 5.1 This email already registered by normal signup or other provider
            user = existingUser.get();
        } else {
            // 5.2.1 Create new User
            if (name == null || name.isEmpty()) {
                // Fallback to email prefix if name is missing
                if (email == null || email.isEmpty()) {
                    throw new RuntimeException("Cannot create user without name or email");
                }
                name = email.split("@")[0];
            }

            // 5.2.2 Ensure username is alphanumeric and lowercase
            String baseUsername = name.toLowerCase().replaceAll("[^a-z0-9]", "");
            String username = baseUsername;

            // 5.2.3 Ensure username uniqueness
            int counter = 1;
            while (userRepository.existsByUsername(username)) {
                username = baseUsername + counter++;
            }

            // 5.2.3 Create and save new user
            user = userRepository.save(User.builder()
                    .username(username)
                    .email(email)
                    .passwordHash(null)
                    .role(UserRole.USER)
                    .emailVerified(true)
                    .provider(provider)
                    .lastLoginAt(Instant.now())
                    .build()
            );
        }

        // 6. Create and save new OAuthAccount
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

    public AuthToken callback(String authToken) {
        // 1. Validate token
        String email = jwtTokenProvider.getUsernameFromToken(authToken);

        // 2. If valid, find user exists by email
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new RuntimeException("User not found");
        }

        // 3. Generate JWT token and return Auth object
        User user = userOpt.get();
        String token = jwtTokenProvider.generateToken(user.getUsername());
        String refreshToken = jwtTokenProvider.generateRefreshToken(user.getUsername());
        Long expiresAt = jwtTokenProvider.getExpirationTimeFromToken(token);

        return new AuthToken(user, token, refreshToken, expiresAt);
    }
}
