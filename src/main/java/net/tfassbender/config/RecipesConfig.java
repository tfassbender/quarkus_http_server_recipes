package net.tfassbender.config;

import io.smallrye.config.ConfigMapping;
import io.smallrye.config.WithDefault;

import java.time.Duration;
import java.util.Optional;

@ConfigMapping(prefix = "recipes")
public interface RecipesConfig {

    /** Directory containing the recipe files; also the local clone target when git is enabled. */
    String path();

    Git git();

    Sync sync();

    interface Git {

        /** Remote URL of the recipe repository. If absent, the directory is used as-is without git. */
        Optional<String> remote();

        /** Branch to check out; defaults to the remote's default branch. */
        Optional<String> branch();

        Optional<String> username();

        /** Password or access token for HTTPS remotes. */
        Optional<String> token();
    }

    interface Sync {

        /** Interval of the periodic pull and index rebuild (read by the scheduler via a config expression). */
        @WithDefault("5m")
        Duration interval();

        /** Minimum time between two manual pulls, shared across all users. */
        @WithDefault("60s")
        Duration manualCooldown();
    }
}
