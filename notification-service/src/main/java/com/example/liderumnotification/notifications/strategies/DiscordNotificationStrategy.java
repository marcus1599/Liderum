package com.example.liderumnotification.notifications.strategies;

import com.example.liderumnotification.messaging.GuildEventCreatedMessage;
import com.example.liderumnotification.notifications.GuildEventCreatedNotificationStrategy;
import com.example.liderumnotification.notifications.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.format.DateTimeFormatter;

@Component
public class DiscordNotificationStrategy implements GuildEventCreatedNotificationStrategy {

    private static final Logger log = LoggerFactory.getLogger(DiscordNotificationStrategy.class);
    private static final DateTimeFormatter EVENT_DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final RestClient restClient;
    private final String webhookUrl;
    private final String username;

    public DiscordNotificationStrategy(
            RestClient restClient,
            @Value("${notification.discord.webhook-url}") String webhookUrl,
            @Value("${notification.discord.username}") String username
    ) {
        this.restClient = restClient;
        this.webhookUrl = webhookUrl;
        this.username = username;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.DISCORD;
    }

    @Override
    public void notify(GuildEventCreatedMessage message) {
        if (webhookUrl.isBlank()) {
            log.warn("Discord notification skipped because DISCORD_WEBHOOK_URL is not configured");
            return;
        }

        restClient.post()
                .uri(webhookUrl)
                .body(new DiscordWebhookRequest(username, buildContent(message)))
                .retrieve()
                .toBodilessEntity();

        log.info("Discord notification sent for eventId={}", message.eventId());
    }

    private String buildContent(GuildEventCreatedMessage message) {
        return """
                **Novo evento criado no Liderum**

                **Evento:** %s
                **Data:** %s
                **ID:** %d
                """.formatted(
                message.eventName(),
                message.date().format(EVENT_DATE_FORMATTER),
                message.eventId()
        );
    }

    private record DiscordWebhookRequest(
            String username,
            String content
    ) {
    }
}
