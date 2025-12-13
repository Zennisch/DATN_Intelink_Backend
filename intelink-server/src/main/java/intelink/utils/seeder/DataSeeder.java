package intelink.utils.seeder;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final SubscriptionPlanSeeder subscriptionPlanSeeder;
    private final UserSeeder userSeeder;

    @Override
    public void run(String... args) throws Exception {
        subscriptionPlanSeeder.seed();
        userSeeder.seed();
    }
}

