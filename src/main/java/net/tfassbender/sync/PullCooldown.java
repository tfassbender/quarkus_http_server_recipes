package net.tfassbender.sync;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global (not per-user) rate limit for manual pulls.
 */
class PullCooldown {

    private final Duration cooldown;
    private final Clock clock;
    private final AtomicReference<Instant> lastAcquired = new AtomicReference<>();

    PullCooldown(Duration cooldown, Clock clock) {
        this.cooldown = cooldown;
        this.clock = clock;
    }

    /**
     * @return empty if the caller may pull now, otherwise the remaining cooldown time
     */
    Optional<Duration> tryAcquire() {
        Instant now = clock.instant();
        Instant last = lastAcquired.get();
        if (last != null) {
            Duration elapsed = Duration.between(last, now);
            if (elapsed.compareTo(cooldown) < 0) {
                return Optional.of(cooldown.minus(elapsed));
            }
        }
        if (lastAcquired.compareAndSet(last, now)) {
            return Optional.empty();
        }
        // a concurrent request acquired it between our read and the CAS, so its cooldown just started
        return Optional.of(cooldown);
    }
}
