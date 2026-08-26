package com.example.Liderum.Security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/** Thread-safe, process-local limiter for the public Guild registration endpoint. */
@Component
public class RegistrationRateLimiter {
    private final int limit;
    private final Duration window;
    private final int maxClients;
    private final Clock clock;
    private final Map<String, Window> clients = new ConcurrentHashMap<>();

    @Autowired
    public RegistrationRateLimiter(
            @Value("${liderum.registration.rate-limit.limit:5}") int limit,
            @Value("${liderum.registration.rate-limit.window:15m}") Duration window,
            @Value("${liderum.registration.rate-limit.max-clients:10000}") int maxClients) {
        this(limit, window, maxClients, Clock.systemUTC());
    }

    RegistrationRateLimiter(int limit, Duration window, int maxClients, Clock clock) {
        if (limit < 1 || window.isZero() || window.isNegative() || maxClients < 1) {
            throw new IllegalArgumentException("Registration rate-limit values must be positive");
        }
        this.limit = limit;
        this.window = window;
        this.maxClients = maxClients;
        this.clock = clock;
    }

    public boolean tryAcquire(String clientKey) {
        Instant now = clock.instant();
        removeExpired(now);
        Window windowState = clients.computeIfAbsent(clientKey, ignored -> {
            if (clients.size() >= maxClients) {
                return null;
            }
            return new Window(now, 0);
        });
        if (windowState == null) {
            return false;
        }
        synchronized (windowState) {
            if (!now.isBefore(windowState.startedAt.plus(window))) {
                windowState.startedAt = now;
                windowState.count = 0;
            }
            if (windowState.count >= limit) {
                return false;
            }
            windowState.count++;
            return true;
        }
    }

    private void removeExpired(Instant now) {
        clients.entrySet().removeIf(entry -> !now.isBefore(entry.getValue().startedAt.plus(window)));
    }

    private static final class Window {
        private Instant startedAt;
        private int count;

        private Window(Instant startedAt, int count) {
            this.startedAt = startedAt;
            this.count = count;
        }
    }
}
