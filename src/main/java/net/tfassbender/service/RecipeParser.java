package net.tfassbender.service;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Extracts the optional tag line and the title from a recipe file's content.
 */
final class RecipeParser {

    private static final Pattern TAG_LINE = Pattern.compile("^<!--\\s*tags:\\s*(.*?)\\s*-->\\s*$");

    record ParsedRecipe(String title, List<String> tags, String markdown) {}

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
        return new ParsedRecipe(title, tags, stripTagLine(content));
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
