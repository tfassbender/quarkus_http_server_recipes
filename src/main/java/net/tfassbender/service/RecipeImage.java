package net.tfassbender.service;

import java.util.Optional;

/**
 * @param path    relative to the recipes' "images" directory, served by {@code /images/{path}}
 * @param caption text shown below the image, or null if none is configured
 */
public record RecipeImage(String path, String caption) {

    private static final String CAPTION_SEPARATOR = "|";

    /**
     * Parses the value of an image metadata line, e.g. "suppe.jpg | Mit frischer Petersilie".
     *
     * @return the image, or empty if the value has no path
     */
    static Optional<RecipeImage> parse(String value) {
        int separator = value.indexOf(CAPTION_SEPARATOR);
        String path = (separator < 0 ? value : value.substring(0, separator)).trim();
        String caption = separator < 0 ? "" : value.substring(separator + 1).trim();
        if (path.isEmpty()) return Optional.empty();
        return Optional.of(new RecipeImage(path, caption.isEmpty() ? null : caption));
    }
}
