package com.example.liderumnotification.notifications;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.rabbitmq.addresses=${RABBITMQ_TEST_ADDRESSES:amqp://guest:guest@localhost:5673}"
})
@EnabledIfSystemProperty(named = "rabbitmq.integration", matches = "true")
class RabbitMqNotificationIntegrationIT {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @MockBean
    private GuildEventCreatedNotificationDispatcher dispatcher;

    @Test
    void shouldDeliverGuildEventCreatedMessageToListener() {
        GuildEventCreatedMessage message = new GuildEventCreatedMessage(9001L, "integration-guild", LocalDateTime.now());
        rabbitTemplate.convertAndSend("liderum.events", "guild.event.created", message);
        verify(dispatcher, timeout(10_000).times(1)).dispatch(any(GuildEventCreatedMessage.class));
    }
}
