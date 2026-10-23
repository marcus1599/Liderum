package com.example.liderumnotification.notifications;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.test.context.SpringBootTest;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "spring.rabbitmq.addresses=${RABBITMQ_TEST_ADDRESSES:amqp://guest:guest@localhost:5673}",
        "notification.channels.enabled=audit"
})
@EnabledIfSystemProperty(named = "rabbitmq.integration", matches = "true")
@EnabledIfEnvironmentVariable(named = "RABBITMQ_TEST_ADDRESSES", matches = "^amqp://.*")
@ExtendWith(OutputCaptureExtension.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RabbitMqNotificationIntegrationIT {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Test
    void shouldProcessGuildEventCreatedMessageThroughAuditLog(CapturedOutput output) throws InterruptedException {
        GuildEventCreatedMessage message = new GuildEventCreatedMessage(
                System.currentTimeMillis(), "integration-guild", LocalDateTime.now());
        rabbitTemplate.convertAndSend("liderum.events", "guild.event.created", message);
        rabbitTemplate.convertAndSend("liderum.events", "guild.event.created", message);

        long deadline = System.nanoTime() + 10_000_000_000L;
        String marker = "eventId=" + message.eventId();
        String duplicateMarker = "Skipping duplicate GuildEventCreated eventId=" + message.eventId();
        while (!output.getOut().contains(duplicateMarker) && System.nanoTime() < deadline) {
            Thread.sleep(50L);
        }

        assertThat(output.getOut())
                .contains("Audit log: GuildEventCreated received")
                .contains(marker)
                .contains(message.eventName())
                .contains(duplicateMarker);
        assertThat(output.getOut().split(java.util.regex.Pattern.quote(marker), -1).length - 1)
                .isEqualTo(2); // one AuditLog line and one duplicate-suppression line
    }
}
