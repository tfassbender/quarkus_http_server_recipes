package net.tfassbender.service;

import jakarta.enterprise.context.ApplicationScoped;
import net.tfassbender.config.RecipesConfig;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves recipe images inside the "images" directory of the recipes path.
 */
@ApplicationScoped
public class ImageService {

    public static final String IMAGES_DIRECTORY = "images";

    // SVG is left out on purpose: it can contain scripts that would run on this origin when opened directly
    private static final Map<String, String> CONTENT_TYPES = Map.of(
            "jpg", "image/jpeg",
            "jpeg", "image/jpeg",
            "png", "image/png",
            "gif", "image/gif",
            "webp", "image/webp",
            "avif", "image/avif");

    public record Image(Path file, String contentType) {}

    private final Path imagesPath;

    public ImageService(RecipesConfig config) {
        this.imagesPath = Path.of(config.path()).resolve(IMAGES_DIRECTORY);
    }

    /**
     * @return the image, or empty if it doesn't exist, isn't a supported image type or lies outside the images directory
     */
    public Optional<Image> findImage(String relativePath) {
        Optional<String> contentType = contentType(relativePath);
        if (contentType.isEmpty()) return Optional.empty();
        try {
            Path file = imagesPath.resolve(relativePath).normalize();
            // real paths also catch symlinks pointing out of the images directory
            if (!Files.isRegularFile(file) || !file.toRealPath().startsWith(imagesPath.toRealPath())) {
                return Optional.empty();
            }
            return Optional.of(new Image(file, contentType.get()));
        } catch (InvalidPathException | IOException e) {
            return Optional.empty();
        }
    }

    private static Optional<String> contentType(String path) {
        int dot = path.lastIndexOf('.');
        if (dot < 0) return Optional.empty();
        return Optional.ofNullable(CONTENT_TYPES.get(path.substring(dot + 1).toLowerCase(Locale.ROOT)));
    }
}
