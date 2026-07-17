package com.example.liderumnotification.notifications;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GuildEventCreatedNotificationDispatcher {

    private static final Logger log = LoggerFactory.getLogger(GuildEventCreatedNotificationDispatcher.class);

    private final NotificationStrategyFactory strategyFactory;

    public GuildEventCreatedNotificationDispatcher(NotificationStrategyFactory strategyFactory) {
        this.strategyFactory = strategyFactory;
    }

    public void dispatch(GuildEventCreatedMessage message) {
        for (GuildEventCreatedNotificationStrategy strategy : strategyFactory.getEnabledStrategies()) {
            try {
                strategy.notify(message);
            } catch (RuntimeException exception) {
                log.error("Notification strategy {} failed for eventId={}",
                        strategy.channel(), message.eventId(), exception);
            }
        }
    }
}
