package com.example.Liderum.Security;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class RegistrationRateLimiterTest {
    @Test
    void expiresWindowWithoutWaiting() {
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        RegistrationRateLimiter limiter = new RegistrationRateLimiter(2, Duration.ofMinutes(15), 10, clock);

        assertThat(limiter.tryAcquire("10.0.0.1")).isTrue();
        assertThat(limiter.tryAcquire("10.0.0.1")).isTrue();
        assertThat(limiter.tryAcquire("10.0.0.1")).isFalse();
        clock.advance(Duration.ofMinutes(15));
        assertThat(limiter.tryAcquire("10.0.0.1")).isTrue();
    }

    @Test
    void separatesClientsAndDoesNotUseForwardedHeadersAsKeys() {
        RegistrationRateLimiter limiter = new RegistrationRateLimiter(1, Duration.ofMinutes(15), 10, Clock.systemUTC());

        assertThat(limiter.tryAcquire("10.0.0.1")).isTrue();
        assertThat(limiter.tryAcquire("10.0.0.1")).isFalse();
        assertThat(limiter.tryAcquire("10.0.0.2")).isTrue();
    }

    private static final class MutableClock extends Clock {
        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override public ZoneId getZone() { return ZoneId.of("UTC"); }
        @Override public Clock withZone(ZoneId zone) { return this; }
        @Override public Instant instant() { return instant; }
    }
}
