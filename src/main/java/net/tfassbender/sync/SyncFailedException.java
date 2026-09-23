package net.tfassbender.sync;

public class SyncFailedException extends RuntimeException {

    public SyncFailedException(Throwable cause) {
        super("Sync failed: " + cause.getMessage(), cause);
    }
}
