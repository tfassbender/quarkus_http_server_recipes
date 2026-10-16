package net.tfassbender.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the optional metadata lines (tags, images), the title and the ingredient section from a recipe file's content.
 */
final class RecipeParser {

    // metadata lines are HTML comments at the very top of the file, so they are neither rendered nor parsed as markdown
    private static final Pattern METADATA_LINE = Pattern.compile("^<!--\\s*(tags|image):\\s*(.*?)\\s*-->\\s*$");
    private static final Pattern INGREDIENTS_HEADING = Pattern.compile("^##\\s+Zutaten\\s*:?$", Pattern.CASE_INSENSITIVE);
    // a level 1 or 2 heading ends the ingredient section; deeper headings (e.g. "### Hefeteig") belong to it
    private static final Pattern SECTION_END = Pattern.compile("^#{1,2}\\s");

    /**
     * @param images      the configured images with their optional captions
     * @param markdown    the content without the metadata lines
     * @param ingredients text of all "## Zutaten" sections, empty if the recipe has none
     */
    record ParsedRecipe(String title, List<String> tags, List<RecipeImage> images, String markdown, String ingredients) {}

    private RecipeParser() {}

    static ParsedRecipe parse(String content) {
        List<String> lines = content.lines().toList();
        int metadataLines = countMetadataLines(lines);
        List<String> tags = new ArrayList<>();
        List<RecipeImage> images = new ArrayList<>();
        for (String line : lines.subList(0, metadataLines)) {
            Matcher m = METADATA_LINE.matcher(line);
            m.matches();
            if (m.group(1).equals("tags")) {
                tags.addAll(parseTags(m.group(2)));
            } else {
                RecipeImage.parse(m.group(2)).ifPresent(images::add);
            }
        }
        String title = lines.stream()
                .skip(metadataLines)
                .map(String::trim)
                .filter(line -> line.startsWith("# "))
                .map(line -> line.substring(2).trim())
                .findFirst()
                .orElse(null);
        return new ParsedRecipe(title, List.copyOf(tags), List.copyOf(images),
                skipLines(content, metadataLines), extractIngredients(lines));
    }

    static String extractIngredients(List<String> lines) {
        StringBuilder ingredients = new StringBuilder();
        boolean inSection = false;
        for (String line : lines) {
            String trimmed = line.trim();
            if (INGREDIENTS_HEADING.matcher(trimmed).matches()) {
                inSection = true;
            } else if (SECTION_END.matcher(trimmed).find()) {
                inSection = false;
            } else if (inSection) {
                ingredients.append(line).append('\n');
            }
        }
        return ingredients.toString();
    }

    static List<String> parseTags(String value) {
        return Arrays.stream(value.split("\\s+"))
                .filter(t -> t.startsWith("#") && t.length() > 1)
                .map(t -> t.substring(1))
                .toList();
    }

    private static int countMetadataLines(List<String> lines) {
        int count = 0;
        while (count < lines.size() && METADATA_LINE.matcher(lines.get(count)).matches()) {
            count++;
        }
        return count;
    }

    /**
     * Removes the first {@code count} lines while keeping the original line endings of the rest.
     */
    private static String skipLines(String content, int count) {
        int start = 0;
        for (int i = 0; i < count; i++) {
            int newline = content.indexOf('\n', start);
            if (newline < 0) return "";
            start = newline + 1;
        }
        return content.substring(start);
    }
}
