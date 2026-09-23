package net.tfassbender.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Immutable snapshot of all recipes, rebuilt as a whole after each sync.
 */
record RecipeIndex(List<RecipeSummary> summaries, Map<String, String> htmlByName) {

    static final RecipeIndex EMPTY = new RecipeIndex(List.of(), Map.of());

    Optional<String> html(String name) {
        return Optional.ofNullable(htmlByName.get(name));
    }
}
