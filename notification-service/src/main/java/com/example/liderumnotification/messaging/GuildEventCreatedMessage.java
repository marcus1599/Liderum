package com.example.liderumnotification.messaging;

import java.time.LocalDateTime;

public record GuildEventCreatedMessage(
        Long eventId,
        String eventName,
        LocalDateTime date
) {
}
