package net.tfassbender.sync;

import io.quarkus.runtime.StartupEvent;
import io.quarkus.scheduler.Scheduled;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;
import net.tfassbender.config.RecipesConfig;
import net.tfassbender.service.RecipesService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.jboss.logging.Logger;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Pulls the recipe repository (if configured) and rebuilds the recipe index: once on startup, periodically,
 * and on manual request. Without a git remote, a sync only re-reads the recipe directory.
 */
@ApplicationScoped
public class SyncService {

    private static final Logger LOG = Logger.getLogger(SyncService.class);

    private final RecipesService recipesService;
    private final Optional<GitRepositoryClient> git;
    private final PullCooldown manualPullCooldown;
    // serializes scheduled and manual syncs, so two pulls never run on the same checkout concurrently
    private final ReentrantLock syncLock = new ReentrantLock();

    private volatile SyncStatus status;

    public SyncService(RecipesConfig config, RecipesService recipesService) {
        this.recipesService = recipesService;
        this.git = config.git().remote().map(remote -> new GitRepositoryClient(Path.of(config.path()), remote,
                config.git().branch(), config.git().username(), config.git().token()));
        this.manualPullCooldown = new PullCooldown(config.sync().manualCooldown(), Clock.systemUTC());
        this.status = new SyncStatus(git.isPresent(), null, null);
    }

    void onStart(@Observes StartupEvent event) {
        trySync();
    }

    @Scheduled(every = "${recipes.sync.interval}", delayed = "${recipes.sync.interval}",
            concurrentExecution = Scheduled.ConcurrentExecution.SKIP)
    void scheduledSync() {
        trySync();
    }

    public SyncStatus status() {
        return status;
    }

    /**
     * @throws PullCooldownException if another manual pull happened within the cooldown period
     * @throws SyncFailedException    if pulling or reading the recipes failed
     */
    public SyncStatus manualSync() {
        Optional<Duration> remainingCooldown = manualPullCooldown.tryAcquire();
        if (remainingCooldown.isPresent()) {
            throw new PullCooldownException(remainingCooldown.get());
        }
        sync();
        return status;
    }

    private void trySync() {
        try {
            sync();
        } catch (SyncFailedException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private void sync() {
        syncLock.lock();
        try {
            if (git.isPresent()) {
                git.get().update();
            }
            recipesService.rebuildIndex();
            status = new SyncStatus(git.isPresent(), Instant.now(), null);
        } catch (IOException | GitAPIException e) {
            // keep serving the previous index; only record the failure
            status = new SyncStatus(git.isPresent(), status.lastSuccess(), e.getMessage());
            throw new SyncFailedException(e);
        } finally {
            syncLock.unlock();
        }
    }
}
