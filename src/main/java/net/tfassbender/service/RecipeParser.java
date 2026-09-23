package net.tfassbender.service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the optional tag line, the title and the ingredient section from a recipe file's content.
 */
final class RecipeParser {

    private static final Pattern TAG_LINE = Pattern.compile("^<!--\\s*tags:\\s*(.*?)\\s*-->\\s*$");
    private static final Pattern INGREDIENTS_HEADING = Pattern.compile("^##\\s+Zutaten\\s*:?$", Pattern.CASE_INSENSITIVE);
    // a level 1 or 2 heading ends the ingredient section; deeper headings (e.g. "### Hefeteig") belong to it
    private static final Pattern SECTION_END = Pattern.compile("^#{1,2}\\s");

    /**
     * @param ingredients text of all "## Zutaten" sections, empty if the recipe has none
     */
    record ParsedRecipe(String title, List<String> tags, String markdown, String ingredients) {}

    private RecipeParser() {}

    static ParsedRecipe parse(String content) {
        List<String> lines = content.lines().toList();
        List<String> tags = lines.isEmpty() ? List.of() : parseTagLine(lines.get(0));
        String title = lines.stream()
                .skip(tags.isEmpty() ? 0 : 1)
                .map(String::trim)
                .filter(line -> line.startsWith("# "))
                .map(line -> line.substring(2).trim())
                .findFirst()
                .orElse(null);
        return new ParsedRecipe(title, tags, stripTagLine(content), extractIngredients(lines));
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
