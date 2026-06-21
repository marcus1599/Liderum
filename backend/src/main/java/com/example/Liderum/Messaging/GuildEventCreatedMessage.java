package com.example.Liderum.Messaging;

import java.time.LocalDateTime;

public record GuildEventCreatedMessage(
        Long eventId,
        String eventName,
        LocalDateTime date
) {
}
