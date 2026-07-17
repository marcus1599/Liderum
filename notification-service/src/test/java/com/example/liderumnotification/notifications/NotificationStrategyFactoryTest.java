package com.example.liderumnotification.notifications;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationStrategyFactoryTest {

    @Test
    void shouldReturnEnabledStrategies() {
        GuildEventCreatedNotificationStrategy auditStrategy = new FakeStrategy(NotificationChannel.AUDIT);
        GuildEventCreatedNotificationStrategy discordStrategy = new FakeStrategy(NotificationChannel.DISCORD);
        NotificationStrategyFactory factory = new NotificationStrategyFactory(
                List.of(auditStrategy, discordStrategy),
                "audit,discord"
        );

        List<GuildEventCreatedNotificationStrategy> strategies = factory.getEnabledStrategies();

        assertThat(strategies)
                .extracting(GuildEventCreatedNotificationStrategy::channel)
                .containsExactly(NotificationChannel.AUDIT, NotificationChannel.DISCORD);
    }

    @Test
    void shouldFallbackToAuditWhenNoValidChannelIsConfigured() {
        GuildEventCreatedNotificationStrategy auditStrategy = new FakeStrategy(NotificationChannel.AUDIT);
        NotificationStrategyFactory factory = new NotificationStrategyFactory(
                List.of(auditStrategy),
                "unknown"
        );

        List<GuildEventCreatedNotificationStrategy> strategies = factory.getEnabledStrategies();

        assertThat(strategies)
                .extracting(GuildEventCreatedNotificationStrategy::channel)
                .containsExactly(NotificationChannel.AUDIT);
    }

    private record FakeStrategy(NotificationChannel channel) implements GuildEventCreatedNotificationStrategy {

        @Override
        public void notify(GuildEventCreatedMessage message) {
        }
    }
}
