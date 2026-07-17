package com.example.liderumnotification.notifications;

import java.util.Locale;
import java.util.Optional;

public enum NotificationChannel {
    AUDIT,
    DISCORD;

    public static Optional<NotificationChannel> from(String value) {
        try {
            return Optional.of(NotificationChannel.valueOf(value.trim().toUpperCase(Locale.ROOT)));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
