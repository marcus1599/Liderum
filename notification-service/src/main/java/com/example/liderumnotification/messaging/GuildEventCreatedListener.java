package com.example.liderumnotification.messaging;

import com.example.liderumnotification.notifications.GuildEventCreatedNotificationDispatcher;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class GuildEventCreatedListener {

    private final GuildEventCreatedNotificationDispatcher notificationDispatcher;

    public GuildEventCreatedListener(GuildEventCreatedNotificationDispatcher notificationDispatcher) {
        this.notificationDispatcher = notificationDispatcher;
    }

    @RabbitListener(queues = "${liderum.rabbitmq.guild-event-created-queue}")
    public void handle(GuildEventCreatedMessage message) {
        notificationDispatcher.dispatch(message);
    }
}
