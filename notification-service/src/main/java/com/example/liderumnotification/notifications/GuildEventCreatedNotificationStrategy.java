package com.example.liderumnotification.notifications;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;

public interface GuildEventCreatedNotificationStrategy {

    NotificationChannel channel();

    void notify(GuildEventCreatedMessage message);
}
