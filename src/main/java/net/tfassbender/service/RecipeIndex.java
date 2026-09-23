package net.tfassbender.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Immutable snapshot of all recipes, rebuilt as a whole after each sync.
 */
record RecipeIndex(List<IndexedRecipe> recipes, Map<String, IndexedRecipe> byName) {

    static final RecipeIndex EMPTY = of(List.of());

    static RecipeIndex of(List<IndexedRecipe> recipes) {
        return new RecipeIndex(List.copyOf(recipes),
                recipes.stream().collect(Collectors.toUnmodifiableMap(IndexedRecipe::name, Function.identity())));
    }

    List<RecipeSummary> summaries() {
        return recipes.stream().map(IndexedRecipe::summary).toList();
    }

    Optional<String> html(String name) {
        return Optional.ofNullable(byName.get(name)).map(IndexedRecipe::html);
    }
}
