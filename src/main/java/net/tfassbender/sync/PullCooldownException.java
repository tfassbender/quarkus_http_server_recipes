package net.tfassbender.sync;

import java.time.Duration;

public class PullCooldownException extends RuntimeException {

    private final Duration remaining;

    public PullCooldownException(Duration remaining) {
        super("Manual pull is on cooldown");
        this.remaining = remaining;
    }

    public long remainingSeconds() {
        // round up, so clients never retry too early
        return (remaining.toMillis() + 999) / 1000;
    }
}
