package net.tfassbender.sync;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Keeps a local checkout in line with a remote repository. The checkout is a read-only mirror: local changes
 * are discarded on every update.
 */
public class GitRepositoryClient {

    private static final String REMOTE_NAME = "origin";

    private final Path localPath;
    private final String remoteUrl;
    private final Optional<String> branch;
    private final CredentialsProvider credentials;

    public GitRepositoryClient(Path localPath, String remoteUrl, Optional<String> branch,
                               Optional<String> username, Optional<String> token) {
        this.localPath = localPath;
        this.remoteUrl = remoteUrl;
        this.branch = branch;
        // Most HTTPS hosts ignore the username when a token is used, but JGit requires a non-empty one
        this.credentials = token
                .map(t -> new UsernamePasswordCredentialsProvider(username.orElse("git"), t))
                .orElse(null);
    }

    /**
     * Clones the repository if there is no local checkout yet, otherwise pulls the latest remote state.
     */
    public void update() throws IOException, GitAPIException {
        if (Files.isDirectory(localPath.resolve(".git"))) {
            pull();
        } else {
            cloneRepository();
        }
    }

    private void cloneRepository() throws IOException, GitAPIException {
        if (Files.exists(localPath) && !isEmptyDirectory(localPath)) {
            throw new IOException("Cannot clone into non-empty directory that is not a git repository: "
                    + localPath.toAbsolutePath());
        }
        try (Git ignored = Git.cloneRepository()
                .setURI(remoteUrl)
                .setDirectory(localPath.toFile())
                .setBranch(branch.orElse(null))
                .setCredentialsProvider(credentials)
                .call()) {
            // clone only, nothing else to do
        }
    }

    private void pull() throws IOException, GitAPIException {
        try (Git git = Git.open(localPath.toFile())) {
            git.fetch()
                    .setRemote(REMOTE_NAME)
                    .setRemoveDeletedRefs(true)
                    .setCredentialsProvider(credentials)
                    .call();

            String branchName = branch.orElse(git.getRepository().getBranch());
            String remoteRef = "refs/remotes/" + REMOTE_NAME + "/" + branchName;
            ObjectId remoteHead = git.getRepository().resolve(remoteRef);
            if (remoteHead == null) {
                throw new IOException("Remote branch not found: " + remoteRef);
            }

            // A hard reset instead of a merge: the server never commits, so the remote state always wins,
            // even after a force-push that rewrote history.
            git.reset().setMode(ResetCommand.ResetType.HARD).setRef(remoteHead.name()).call();
            git.clean().setCleanDirectories(true).setForce(true).call();
        }
    }

    private static boolean isEmptyDirectory(Path path) throws IOException {
        if (!Files.isDirectory(path)) return false;
        try (Stream<Path> entries = Files.list(path)) {
            return entries.findAny().isEmpty();
        }
    }
}
