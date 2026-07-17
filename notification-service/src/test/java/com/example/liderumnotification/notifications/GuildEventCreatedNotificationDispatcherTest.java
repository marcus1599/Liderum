package com.example.liderumnotification.notifications;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.assertj.core.api.Assertions.assertThat;

class GuildEventCreatedNotificationDispatcherTest {

    @Test
    void shouldContinueDispatchingWhenOneStrategyFails() {
        RecordingStrategy auditStrategy = new RecordingStrategy(NotificationChannel.AUDIT);
        FailingStrategy discordStrategy = new FailingStrategy(NotificationChannel.DISCORD);
        NotificationStrategyFactory factory = new NotificationStrategyFactory(
                List.of(discordStrategy, auditStrategy),
                "discord,audit"
        );
        GuildEventCreatedNotificationDispatcher dispatcher = new GuildEventCreatedNotificationDispatcher(factory);

        dispatcher.dispatch(new GuildEventCreatedMessage(
                1L,
                "TW",
                LocalDateTime.of(2026, 7, 4, 20, 0)
        ));

        assertThat(auditStrategy.wasCalled()).isTrue();
    }

    private record FailingStrategy(NotificationChannel channel) implements GuildEventCreatedNotificationStrategy {

        @Override
        public void notify(GuildEventCreatedMessage message) {
            throw new RuntimeException("Strategy failed");
        }
    }

    private static class RecordingStrategy implements GuildEventCreatedNotificationStrategy {

        private final NotificationChannel channel;
        private final AtomicBoolean called = new AtomicBoolean(false);

        private RecordingStrategy(NotificationChannel channel) {
            this.channel = channel;
        }

        @Override
        public NotificationChannel channel() {
            return channel;
        }

        @Override
        public void notify(GuildEventCreatedMessage message) {
            called.set(true);
        }

        private boolean wasCalled() {
            return called.get();
        }
    }
}
