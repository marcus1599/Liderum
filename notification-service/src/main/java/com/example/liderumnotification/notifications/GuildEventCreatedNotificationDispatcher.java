package com.example.liderumnotification.notifications;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class GuildEventCreatedNotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(GuildEventCreatedNotificationDispatcher.class);

    private final NotificationStrategyFactory strategyFactory;
    private final Set<Long> processedEventIds = ConcurrentHashMap.newKeySet();

    public GuildEventCreatedNotificationDispatcher(NotificationStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public void dispatch(GuildEventCreatedMessage message) {
        if (!processedEventIds.add(message.eventId())) {
            log.info("Skipping duplicate GuildEventCreated eventId={}", message.eventId());
            return;
        }
        for (GuildEventCreatedNotificationStrategy strategy : strategyFactory.getEnabledStrategies()) {
            try {
                strategy.notify(message);
            } catch (RuntimeException exception) {
                processedEventIds.remove(message.eventId());
                log.error("Notification strategy {} failed for eventId={}",
                        strategy.channel(), message.eventId(), exception);
                throw exception;
            }
        }
    }
}
