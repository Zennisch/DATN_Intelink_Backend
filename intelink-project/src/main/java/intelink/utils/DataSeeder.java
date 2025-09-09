package intelink.utils;

import intelink.models.ShortUrl;
import intelink.models.SubscriptionPlan;
import intelink.models.User;
import intelink.repositories.UserRepository;
import intelink.utils.seeder.*;
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
//    private final SubscriptionDataSeeder subscriptionDataSeeder;
    private final ApiKeyDataSeeder apiKeyDataSeeder;
    private final CustomDomainDataSeeder customDomainDataSeeder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Data already exists, skipping seeding");
            return;
        }

        log.info("Starting data seeding...");

        // Create users first
        List<User> users = userDataSeeder.createUsers(100);
        log.info("Created {} users", users.size());

        // Create short URLs
        List<ShortUrl> shortUrls = shortUrlDataSeeder.createShortUrls(users, 500);
        log.info("Created {} short URLs", shortUrls.size());

        // Create security data
        securityDataSeeder.createVerificationTokens(users, 50);
        securityDataSeeder.createOAuthAccounts(users, 30);
        log.info("Created security data");

        // Create analysis results
        analysisDataSeeder.createAnalysisResults(shortUrls, 200);
        log.info("Created analysis results");

        // Create click logs and statistics
        clickDataSeeder.createClickLogsAndStats(shortUrls, 50000);
        log.info("Created click logs and statistics");

        // Create API keys
        apiKeyDataSeeder.createApiKeys(users, 40);
        log.info("Created API keys");

        // Create custom domains
        customDomainDataSeeder.createCustomDomains(users, 25);
        log.info("Created custom domains");

        // Create subscription data
//        List<SubscriptionPlan> subscriptionPlans = subscriptionDataSeeder.createSubscriptionPlans();
//        subscriptionDataSeeder.createSubscriptions(users, subscriptionPlans, 30);
        log.info("Created subscription data");

        log.info("Data seeding completed successfully!");
    }
}
