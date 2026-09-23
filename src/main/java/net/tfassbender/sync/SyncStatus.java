package net.tfassbender.sync;

import java.time.Instant;

/**
 * @param lastSuccess time of the last successful sync, null if there was none yet
 * @param lastError   error message of the most recent sync attempt, null if it succeeded
 */
public record SyncStatus(boolean gitEnabled, Instant lastSuccess, String lastError) {}
