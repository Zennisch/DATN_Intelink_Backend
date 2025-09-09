package intelink.utils.seeder;

import intelink.models.CustomDomain;
import intelink.models.User;
import intelink.models.enums.CustomDomainStatus;
import intelink.repositories.CustomDomainRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomDomainDataSeeder {

    private final CustomDomainRepository customDomainRepository;
    private final DataSeedingUtils utils;

    public void createCustomDomains(List<User> users, int count) {
        log.info("Creating {} custom domains...", count);
        List<CustomDomain> domains = new ArrayList<>();

        String[] baseDomains = {
            "techcorp", "innovate", "digitalart", "webcraft", "linkpro", 
            "smarturl", "quicklink", "brandify", "netforce", "pixelco"
        };
        
        String[] tlds = {".com", ".net", ".org", ".io", ".tech", ".app"};

        for (int i = 0; i < count; i++) {
            User randomUser = utils.getRandomElement(users);
            String domain = utils.getRandomElement(List.of(baseDomains)) + 
                           utils.getRandom().nextInt(1000) + 
                           utils.getRandomElement(List.of(tlds));
            
            Instant createdAt = utils.getRandomInstantBetween(2024, 2025);
            CustomDomainStatus status = utils.getRandomDomainStatus();
            boolean verified = status == CustomDomainStatus.VERIFIED;

            CustomDomain customDomain = CustomDomain.builder()
                    .domain(domain)
                    .subdomain(utils.getRandom().nextDouble() < 0.3 ? "links" : null)
                    .status(status)
                    .verificationToken(UUID.randomUUID().toString())
                    .verificationMethod(utils.getRandomVerificationMethod())
                    .verified(verified)
                    .sslEnabled(verified && utils.getRandom().nextDouble() < 0.8)
                    .active(utils.getRandom().nextDouble() < 0.9)
                    .createdAt(createdAt)
                    .updatedAt(utils.getRandomInstantAfter(createdAt))
                    .user(randomUser)
                    .build();

            domains.add(customDomain);
        }

        customDomainRepository.saveAll(domains);
    }
}
