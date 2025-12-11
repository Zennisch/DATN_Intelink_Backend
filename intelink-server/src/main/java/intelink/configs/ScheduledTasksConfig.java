package intelink.configs;

import intelink.modules.subscription.services.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
@Slf4j
public class ScheduledTasksConfig {

    private final SubscriptionService subscriptionService;

    /**
     * Clean up expired subscriptions every day at 2 AM
     */
    @Scheduled(cron = "0 0 2 * * *")
    public void cleanupExpiredSubscriptions() {
        log.info("[ScheduledTasks] Starting cleanup of expired subscriptions");
        int count = subscriptionService.cleanupExpiredSubscriptions();
        log.info("[ScheduledTasks] Completed cleanup: {} expired subscriptions processed", count);
    }

    /**
     * Clean up old pending subscriptions every 6 hours
     */
    @Scheduled(cron = "0 0 */6 * * *")
    public void cleanupOldPendingSubscriptions() {
        log.info("[ScheduledTasks] Starting cleanup of old pending subscriptions");
        int count = subscriptionService.cleanupOldPendingSubscriptions();
        log.info("[ScheduledTasks] Completed cleanup: {} pending subscriptions canceled", count);
    }
}
