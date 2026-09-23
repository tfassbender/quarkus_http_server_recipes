package net.tfassbender.sync;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GitRepositoryClientTest {

    @TempDir
    Path tempDir;

    private Path originPath;
    private Path checkoutPath;
    private Git origin;
    private GitRepositoryClient client;

    @BeforeEach
    void setUp() throws GitAPIException, IOException {
        originPath = tempDir.resolve("origin");
        checkoutPath = tempDir.resolve("checkout");
        origin = Git.init().setDirectory(originPath.toFile()).setInitialBranch("main").call();
        commitFile("pizza.txt", "# Pizza");
        client = new GitRepositoryClient(checkoutPath, originPath.toUri().toString(),
                Optional.empty(), Optional.empty(), Optional.empty());
    }

    @AfterEach
    void tearDown() {
        origin.close();
    }

    @Test
    void update_clonesIfMissing() throws Exception {
        client.update();

        assertEquals("# Pizza", Files.readString(checkoutPath.resolve("pizza.txt")));
    }

    @Test
    void update_pullsNewAndDeletedFiles() throws Exception {
        client.update();
        commitFile("pasta.txt", "# Pasta");
        origin.rm().addFilepattern("pizza.txt").call();
        origin.commit().setMessage("remove pizza").call();

        client.update();

        assertEquals("# Pasta", Files.readString(checkoutPath.resolve("pasta.txt")));
        assertFalse(Files.exists(checkoutPath.resolve("pizza.txt")));
    }

    @Test
    void update_discardsLocalChanges() throws Exception {
        client.update();
        Files.writeString(checkoutPath.resolve("pizza.txt"), "local edit");
        Files.writeString(checkoutPath.resolve("untracked.txt"), "local file");

        client.update();

        assertEquals("# Pizza", Files.readString(checkoutPath.resolve("pizza.txt")));
        assertFalse(Files.exists(checkoutPath.resolve("untracked.txt")));
    }

    @Test
    void update_refusesToCloneIntoNonEmptyDirectory() throws IOException {
        Files.createDirectories(checkoutPath);
        Files.writeString(checkoutPath.resolve("existing.txt"), "keep me");

        assertThrows(IOException.class, client::update);
        assertTrue(Files.exists(checkoutPath.resolve("existing.txt")));
    }

    private void commitFile(String name, String content) throws IOException, GitAPIException {
        Files.writeString(originPath.resolve(name), content);
        origin.add().addFilepattern(name).call();
        origin.commit().setMessage("add " + name).call();
    }
}
