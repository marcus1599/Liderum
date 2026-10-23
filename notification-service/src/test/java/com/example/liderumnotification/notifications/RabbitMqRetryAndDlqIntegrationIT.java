package com.example.liderumnotification.notifications;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import com.example.liderumnotification.notifications.strategies.AuditLogNotificationStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.timeout;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.rabbitmq.addresses=${RABBITMQ_TEST_ADDRESSES:amqp://guest:guest@localhost:5673}",
        "notification.channels.enabled=audit"
})
@EnabledIfSystemProperty(named = "rabbitmq.integration", matches = "true")
@EnabledIfEnvironmentVariable(named = "RABBITMQ_TEST_ADDRESSES", matches = "^amqp://.*")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RabbitMqRetryAndDlqIntegrationIT {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    // The strategy is the intentional failure boundary; listener, dispatcher, retry advice,
    // RabbitMQ topology and dead-letter routing remain real.
    @SpyBean
    private AuditLogNotificationStrategy auditLogStrategy;

    @Test
    void retriesThreeTimesThenDeadLettersWithoutRequeueLoop() {
        GuildEventCreatedMessage message = new GuildEventCreatedMessage(
                System.currentTimeMillis(), "retry-dlq-integration", LocalDateTime.now());
        doThrow(new IllegalStateException("synthetic strategy failure"))
                .when(auditLogStrategy).notify(any(GuildEventCreatedMessage.class));

        rabbitTemplate.convertAndSend("liderum.events", "guild.event.created", message);

        verify(auditLogStrategy, timeout(15_000).times(3))
                .notify(any(GuildEventCreatedMessage.class));

        Object deadLetter = rabbitTemplate.receiveAndConvert("notification.guild-event-created.dlq", 10_000);
        assertThat(deadLetter).isInstanceOf(GuildEventCreatedMessage.class);
        assertThat((GuildEventCreatedMessage) deadLetter)
                .usingRecursiveComparison()
                .isEqualTo(message);

        verify(auditLogStrategy, times(3)).notify(any(GuildEventCreatedMessage.class));
        assertThat(rabbitTemplate.receive("notification.guild-event-created.dlq", 500L)).isNull();
    }
}
