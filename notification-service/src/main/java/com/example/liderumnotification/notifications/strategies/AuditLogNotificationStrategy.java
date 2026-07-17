package com.example.liderumnotification.notifications.strategies;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import com.example.liderumnotification.notifications.GuildEventCreatedNotificationStrategy;
import com.example.liderumnotification.notifications.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class AuditLogNotificationStrategy implements GuildEventCreatedNotificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(AuditLogNotificationStrategy.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.AUDIT;
    }

    @Override
    public void notify(GuildEventCreatedMessage message) {
        log.info("Audit log: GuildEventCreated received. eventId={}, eventName={}, date={}",
                message.eventId(), message.eventName(), message.date());
    }
}
