package intelink.utils;

import intelink.models.ShortUrl;
import intelink.models.User;
import intelink.repositories.*;
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

    // Repositories
    private final UserRepository userRepository;
    private final ShortUrlRepository shortUrlRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final OAuthAccountRepository oAuthAccountRepository;
    private final ShortUrlAnalysisResultRepository shortUrlAnalysisResultRepository;
    private final ClickLogRepository clickLogRepository;
    private final ApiKeyRepository apiKeyRepository;
    private final CustomDomainRepository customDomainRepository;

    // Seeders
    private final UserDataSeeder userDataSeeder;
    private final ShortUrlDataSeeder shortUrlDataSeeder;
    private final SubscriptionPlanDataSeeder subscriptionPlanDataSeeder;
    private final SecurityDataSeeder securityDataSeeder;
    private final AnalysisDataSeeder analysisDataSeeder;
    private final ClickDataSeeder clickDataSeeder;
    private final ApiKeyDataSeeder apiKeyDataSeeder;
    private final CustomDomainDataSeeder customDomainDataSeeder;

    @Override
    @Transactional
    public void run(String... args) {
        log.info("Starting data seeding...");

        // 1. Seed Users
        List<User> users;
        if (userRepository.count() == 0) {
            users = userDataSeeder.createUsers(10);
            log.info("Created {} users", users.size());
        } else {
            users = userRepository.findAll();
            log.info("Users already exist, using existing {} users", users.size());
        }

        // 2. Seed Short URLs
        List<ShortUrl> shortUrls;
        if (shortUrlRepository.count() == 0) {
            shortUrls = shortUrlDataSeeder.createShortUrls(users, 20);
            log.info("Created {} short URLs", shortUrls.size());
        } else {
            shortUrls = shortUrlRepository.findAll();
            log.info("Short URLs already exist, using existing {} short URLs", shortUrls.size());
        }

        // 3. Seed Security Data
        if (verificationTokenRepository.count() == 0 || oAuthAccountRepository.count() == 0) {
            securityDataSeeder.createVerificationTokens(users, 50);
            securityDataSeeder.createOAuthAccounts(users, 30);
            log.info("Created security data");
        } else {
            log.info("Security data already exists, skipping");
        }

        // 4. Seed Analysis Results
        if (shortUrlAnalysisResultRepository.count() == 0) {
            analysisDataSeeder.createAnalysisResults(shortUrls, 200);
            log.info("Created analysis results");
        } else {
            log.info("Analysis results already exist, skipping");
        }

        // 5. Seed Click Logs and Stats
        if (clickLogRepository.count() == 0) {
            clickDataSeeder.createClickLogsAndStats(shortUrls, 50000);
            log.info("Created click logs and statistics");
        } else {
            log.info("Click logs already exist, skipping");
        }

        // 6. Seed API Keys
//        if (apiKeyRepository.count() == 0) {
//            apiKeyDataSeeder.createApiKeys(users, 40);
//            log.info("Created API keys");
//        } else {
//            log.info("API keys already exist, skipping");
//        }

        // 7. Seed Custom Domains
        if (customDomainRepository.count() == 0) {
            customDomainDataSeeder.createCustomDomains(users, 25);
            log.info("Created custom domains");
        } else {
            log.info("Custom domains already exist, skipping");
        }

        // 8. Seed Subscription Plans
        if (subscriptionPlanRepository.count() == 0) {
            subscriptionPlanDataSeeder.createDefaultPlans();
            log.info("Created default subscription plans");
        } else {
            log.info("Subscription plans already exist, skipping");
        }

        log.info("Data seeding completed successfully!");
    }
}
