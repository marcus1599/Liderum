package com.example.liderumnotification.notifications;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class NotificationStrategyFactory {

    private static final Logger log = LoggerFactory.getLogger(NotificationStrategyFactory.class);

    private final Map<NotificationChannel, GuildEventCreatedNotificationStrategy> strategiesByChannel;
    private final EnumSet<NotificationChannel> enabledChannels;

    public NotificationStrategyFactory(
            List<GuildEventCreatedNotificationStrategy> strategies,
            @Value("${notification.channels.enabled}") String enabledChannels
    ) {
        this.strategiesByChannel = strategies.stream()
                .collect(Collectors.toUnmodifiableMap(
                        GuildEventCreatedNotificationStrategy::channel,
                        Function.identity()
                ));
        this.enabledChannels = parseEnabledChannels(enabledChannels);
    }

    public List<GuildEventCreatedNotificationStrategy> getEnabledStrategies() {
        return enabledChannels.stream()
                .map(strategiesByChannel::get)
                .filter(strategy -> strategy != null)
                .toList();
    }

    private EnumSet<NotificationChannel> parseEnabledChannels(String rawChannels) {
        EnumSet<NotificationChannel> channels = EnumSet.noneOf(NotificationChannel.class);

        for (String rawChannel : rawChannels.split(",")) {
            NotificationChannel.from(rawChannel)
                    .ifPresentOrElse(
                            channels::add,
                            () -> log.warn("Ignoring unknown notification channel '{}'", rawChannel)
                    );
        }

        return channels.isEmpty() ? EnumSet.of(NotificationChannel.AUDIT) : channels;
    }
}
