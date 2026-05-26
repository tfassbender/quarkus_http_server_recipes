package net.tfassbender.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import net.tfassbender.markdown.RecipesMarkdownFormatter;
import org.eclipse.microprofile.config.inject.ConfigProperty;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@ApplicationScoped
public class RecipesService {

    private static final String EXTENSION = ".txt";
    private static final Pattern TAG_LINE = Pattern.compile("^<!--\\s*tags:\\s*(.*?)\\s*-->\\s*$");

    @ConfigProperty(name = "recipes.path")
    private String recipesPath;

    @Inject
    RecipesMarkdownFormatter formatter;

    private final ConcurrentHashMap<String, CacheEntry> htmlCache = new ConcurrentHashMap<>();

    private record CacheEntry(FileTime mtime, String html) {}

    public List<RecipeSummary> listRecipeFiles() throws IOException {
        Path path = Paths.get(recipesPath);

        if (!Files.exists(path) || !Files.isDirectory(path)) {
            throw new IOException("Invalid recipes path: " + path.toAbsolutePath());
        }

        try (Stream<Path> files = Files.list(path)) {
            List<RecipeSummary> summaries = new ArrayList<>();
            files
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(EXTENSION))
                    .sorted()
                    .forEach(p -> summaries.add(toSummary(p)));
            return summaries;
        }
    }

    public String getRecipeHtml(String filename) throws IOException {
        Path path = resolveSafely(filename);

        if (!Files.exists(path) || !Files.isRegularFile(path)) {
            throw new IOException("File not found: " + path.toAbsolutePath());
        }

        FileTime mtime = Files.getLastModifiedTime(path);
        CacheEntry cached = htmlCache.get(filename);
        if (cached != null && cached.mtime().equals(mtime)) {
            return cached.html();
        }

        String content = Files.readString(path);
        String html = formatter.toHtml(stripTagLine(content));
        htmlCache.put(filename, new CacheEntry(mtime, html));
        return html;
    }

    private Path resolveSafely(String filename) throws IOException {
        if (filename == null || filename.isBlank()
                || filename.contains("/") || filename.contains("\\")
                || filename.contains("..") || filename.startsWith(".")) {
            throw new IOException("Invalid filename: " + filename);
        }
        Path base = Paths.get(recipesPath).toAbsolutePath().normalize();
        Path resolved = base.resolve(filename + EXTENSION).normalize();
        // belt-and-suspenders: ensure we stay under the base directory
        if (!resolved.startsWith(base)) {
            throw new IOException("Invalid filename: " + filename);
        }
        return resolved;
    }

    private RecipeSummary toSummary(Path file) {
        String name = file.getFileName().toString();
        name = name.substring(0, name.length() - EXTENSION.length());
        List<String> tags = readTags(file);
        return new RecipeSummary(name, tags);
    }

    private List<String> readTags(Path file) {
        try (BufferedReader reader = Files.newBufferedReader(file)) {
            String firstLine = reader.readLine();
            return parseTagLine(firstLine);
        } catch (IOException e) {
            return List.of();
        }
    }

    static List<String> parseTagLine(String line) {
        if (line == null) return List.of();
        Matcher m = TAG_LINE.matcher(line);
        if (!m.matches()) return List.of();
        return Arrays.stream(m.group(1).split("\\s+"))
                .filter(t -> t.startsWith("#") && t.length() > 1)
                .map(t -> t.substring(1))
                .toList();
    }

    static String stripTagLine(String content) {
        int newline = content.indexOf('\n');
        String firstLine = newline < 0 ? content : content.substring(0, newline);
        // tolerate trailing \r on Windows
        String trimmed = firstLine.endsWith("\r") ? firstLine.substring(0, firstLine.length() - 1) : firstLine;
        if (!TAG_LINE.matcher(trimmed).matches()) {
            return content;
        }
        return newline < 0 ? "" : content.substring(newline + 1);
    }
}
