package com.example.liderumnotification.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class GuildEventCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(GuildEventCreatedListener.class);

    @RabbitListener(queues = "${liderum.rabbitmq.guild-event-created-queue}")
    public void handle(GuildEventCreatedMessage message) {
        log.info("Audit log: GuildEventCreated received. eventId={}, eventName={}, date={}",
                message.eventId(), message.eventName(), message.date());
        log.info("Notification scheduled for guild event '{}'", message.eventName());
    }
}
