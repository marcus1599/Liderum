package com.example.Liderum;

import com.example.Liderum.Messaging.GuildEventCreatedMessage;
import com.example.Liderum.Messaging.GuildEventCreatedPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class GuildEventCreatedPublisherTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    private GuildEventCreatedPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new GuildEventCreatedPublisher(rabbitTemplate);
        ReflectionTestUtils.setField(publisher, "exchangeName", "liderum.events");
        ReflectionTestUtils.setField(publisher, "routingKey", "guild.event.created");
    }

    @Test
    void shouldKeepNotificationBestEffortAndLogFailureWhenRabbitMqIsUnavailable(CapturedOutput output) {
        GuildEventCreatedMessage message = new GuildEventCreatedMessage(
                77L, "synthetic unavailable broker", LocalDateTime.of(2026, 10, 21, 12, 0));
        doThrow(new AmqpConnectException(new IOException("synthetic broker unavailable")))
                .when(rabbitTemplate)
                .convertAndSend("liderum.events", "guild.event.created", message);

        assertThatCode(() -> publisher.publish(message)).doesNotThrowAnyException();

        verify(rabbitTemplate).convertAndSend("liderum.events", "guild.event.created", message);
        assertThat(output.getOut())
                .contains("Could not publish GuildEventCreated message for eventId=77")
                .doesNotContain("Published GuildEventCreated message for eventId=77");
    }
}
