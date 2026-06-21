package com.example.Liderum.Messaging;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GuildEventCreatedPublisher {

    private final RabbitTemplate rabbitTemplate;

    @Value("${liderum.rabbitmq.exchange}")
    private String exchangeName;

    @Value("${liderum.rabbitmq.guild-event-created-routing-key}")
    private String routingKey;

    public void publish(GuildEventCreatedMessage message) {
        try {
            rabbitTemplate.convertAndSend(exchangeName, routingKey, message);
            log.info("Published GuildEventCreated message for eventId={}", message.eventId());
        } catch (AmqpException exception) {
            log.warn("Could not publish GuildEventCreated message for eventId={}", message.eventId(), exception);
        }
    }
}
