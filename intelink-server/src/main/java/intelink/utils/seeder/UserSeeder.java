package intelink.utils.seeder;

import intelink.models.User;
import intelink.models.enums.UserRole;
import intelink.models.enums.UserStatus;
import intelink.modules.auth.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class UserSeeder {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public void seed() {
        if (userRepository.count() == 0) {
            List<User> users = new ArrayList<>();
            String commonPassword = passwordEncoder.encode("password");
            Random random = new Random();

            for (int i = 1; i <= 5; i++) {
                User.UserBuilder userBuilder = User.builder()
                        .username("user" + i)
                        .email("user" + i + "@example.com")
                        .password(commonPassword)
                        .profileName("User " + i)
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now());

                if (i == 1) {
                    // User 1: Verified, Role USER
                    userBuilder.verified(true)
                            .role(UserRole.USER)
                            .status(UserStatus.ACTIVE);
                } else if (i == 2) {
                    // User 2: Role ADMIN (Assume verified and active)
                    userBuilder.verified(true)
                            .role(UserRole.ADMIN)
                            .status(UserStatus.ACTIVE);
                } else {
                    // Random data for others
                    boolean isVerified = random.nextBoolean();
                    userBuilder.verified(isVerified);
                    
                    // Random Role (80% USER, 20% ADMIN)
                    userBuilder.role(random.nextInt(10) < 8 ? UserRole.USER : UserRole.ADMIN);

                    // Random Status (80% ACTIVE, 10% INACTIVE, 10% BANNED)
                    int statusRandom = random.nextInt(10);
                    if (statusRandom < 8) {
                        userBuilder.status(UserStatus.ACTIVE);
                    } else if (statusRandom < 9) {
                        userBuilder.status(UserStatus.INACTIVE);
                    } else {
                        userBuilder.status(UserStatus.BANNED);
                    }
                }

                users.add(userBuilder.build());
            }

            userRepository.saveAll(users);
        }
    }
}
