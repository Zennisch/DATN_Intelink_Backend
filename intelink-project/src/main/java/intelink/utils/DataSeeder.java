package intelink.utils;

import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.repositories.UserRepository;
import intelink.utils.dataseeding.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final UserDataSeeder userDataSeeder;
    private final ShortUrlDataSeeder shortUrlDataSeeder;
    private final SecurityDataSeeder securityDataSeeder;
    private final AnalysisDataSeeder analysisDataSeeder;
    private final ClickDataSeeder clickDataSeeder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (userRepository.count() > 0) {
            log.info("Data already exists, skipping seeding");
            return;
        }

        log.info("Starting data seeding...");

        List<User> users = userDataSeeder.createUsers(100);
        log.info("Created {} users", users.size());

        List<ShortUrl> shortUrls = shortUrlDataSeeder.createShortUrls(users, 500);
        log.info("Created {} short URLs", shortUrls.size());

        securityDataSeeder.createVerificationTokens(users, 50);
        securityDataSeeder.createOAuthAccounts(users, 30);
        log.info("Created security data");

        analysisDataSeeder.createAnalysisResults(shortUrls, 200);
        log.info("Created analysis results");

        clickDataSeeder.createClickLogsAndStats(shortUrls, 50000);
        log.info("Created click logs and statistics");

        log.info("Data seeding completed successfully!");
    }
}
