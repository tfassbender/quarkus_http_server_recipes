package net.tfassbender.sync;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PullCooldownTest {

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T12:00:00Z"));
    private final PullCooldown cooldown = new PullCooldown(Duration.ofSeconds(60), clock);

    @Test
    void firstPull_isAllowed() {
        assertTrue(cooldown.tryAcquire().isEmpty());
    }

    @Test
    void secondPullWithinCooldown_isRejectedWithRemainingTime() {
        cooldown.tryAcquire();
        clock.advance(Duration.ofSeconds(20));

        Optional<Duration> remaining = cooldown.tryAcquire();

        assertEquals(Optional.of(Duration.ofSeconds(40)), remaining);
    }

    @Test
    void rejectedPull_doesNotExtendCooldown() {
        cooldown.tryAcquire();
        clock.advance(Duration.ofSeconds(30));
        cooldown.tryAcquire();
        clock.advance(Duration.ofSeconds(30));

        assertTrue(cooldown.tryAcquire().isEmpty());
    }

    @Test
    void pullAfterCooldown_isAllowedAndRestartsCooldown() {
        cooldown.tryAcquire();
        clock.advance(Duration.ofSeconds(60));

        assertTrue(cooldown.tryAcquire().isEmpty());
        assertEquals(Optional.of(Duration.ofSeconds(60)), cooldown.tryAcquire());
    }

    private static final class MutableClock extends Clock {

        private Instant now;

        MutableClock(Instant start) {
            this.now = start;
        }

        void advance(Duration duration) {
            now = now.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
