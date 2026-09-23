package net.tfassbender.service;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Ranks recipes by how many of the searched ingredients appear in their "## Zutaten" section (see
 * {@link IngredientTerm} for the matching rules). Terms starting with '#' are tags: recipes having all of them
 * are listed first, the others separately.
 */
final class IngredientSearch {

    private static final Pattern TERM_SEPARATOR = Pattern.compile("[\\s,;]+");
    private static final Pattern WORD = Pattern.compile("[\\p{L}\\p{N}]+");

    private IngredientSearch() {}

    record Query(List<String> ingredients, List<String> tags) {

        static Query parse(String raw) {
            Set<String> ingredients = new LinkedHashSet<>();
            Set<String> tags = new LinkedHashSet<>();
            String normalized = raw == null ? "" : raw.trim().toLowerCase(Locale.ROOT);
            for (String term : TERM_SEPARATOR.split(normalized)) {
                if (term.startsWith("#")) {
                    if (term.length() > 1) tags.add(term.substring(1));
                } else if (!term.replace(IngredientTerm.WILDCARD, "").isEmpty()) {
                    ingredients.add(term);
                }
            }
            return new Query(List.copyOf(ingredients), List.copyOf(tags));
        }

        boolean isEmpty() {
            return ingredients.isEmpty() && tags.isEmpty();
        }
    }

    static IngredientSearchResponse search(List<IndexedRecipe> recipes, Query query) {
        if (query.isEmpty()) {
            return IngredientSearchResponse.EMPTY;
        }
        List<IngredientTerm> terms = query.ingredients().stream().map(IngredientTerm::parse).toList();
        List<IngredientSearchResult> resultsWithTags = new ArrayList<>();
        List<IngredientSearchResult> resultsMissingTags = new ArrayList<>();

        for (IndexedRecipe recipe : recipes) {
            List<String> matched = terms.stream()
                    .filter(term -> term.matchesAny(recipe.ingredientWords()))
                    .map(IngredientTerm::text)
                    .toList();
            boolean hasAllTags = hasAllTags(recipe, query.tags());
            // with only tags searched, every tagged recipe is a result; otherwise at least one ingredient must match
            if (hasAllTags && (!matched.isEmpty() || query.ingredients().isEmpty())) {
                resultsWithTags.add(toResult(recipe, matched));
            } else if (!hasAllTags && !matched.isEmpty()) {
                resultsMissingTags.add(toResult(recipe, matched));
            }
        }
        return new IngredientSearchResponse(query.ingredients().size(), query.tags(),
                sorted(resultsWithTags), sorted(resultsMissingTags));
    }

    /**
     * Splits an ingredient section into lowercase words, e.g. "| 1 EL | Olivenöl |" into [1, el, olivenöl].
     */
    static Set<String> ingredientWords(String text) {
        return WORD.matcher(text.toLowerCase(Locale.ROOT)).results()
                .map(MatchResult::group)
                .collect(Collectors.toUnmodifiableSet());
    }

    private static boolean hasAllTags(IndexedRecipe recipe, List<String> tags) {
        List<String> recipeTags = recipe.summary().tags().stream().map(t -> t.toLowerCase(Locale.ROOT)).toList();
        return recipeTags.containsAll(tags);
    }

    private static IngredientSearchResult toResult(IndexedRecipe recipe, List<String> matched) {
        RecipeSummary summary = recipe.summary();
        return new IngredientSearchResult(summary.name(), summary.title(), matched.size(), List.copyOf(matched));
    }

    private static List<IngredientSearchResult> sorted(List<IngredientSearchResult> results) {
        // Collator instances are not thread-safe, so create one per search
        Collator collator = Collator.getInstance(Locale.GERMAN);
        Comparator<IngredientSearchResult> byName = Comparator.comparing(
                r -> r.title() != null ? r.title() : r.name(), collator);
        return results.stream()
                .sorted(Comparator.comparingInt(IngredientSearchResult::score).reversed().thenComparing(byName))
                .toList();
    }
}
