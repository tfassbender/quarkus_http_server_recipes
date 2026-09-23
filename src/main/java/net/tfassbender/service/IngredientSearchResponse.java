package net.tfassbender.service;

import java.util.List;

/**
 * @param ingredientCount    number of distinct ingredients in the query
 * @param tags               the searched tags (without '#')
 * @param results            recipes that have all searched tags (all matches if no tag was searched)
 * @param resultsMissingTags recipes that match ingredients but lack at least one searched tag
 */
public record IngredientSearchResponse(int ingredientCount, List<String> tags,
                                       List<IngredientSearchResult> results,
                                       List<IngredientSearchResult> resultsMissingTags) {

    static final IngredientSearchResponse EMPTY = new IngredientSearchResponse(0, List.of(), List.of(), List.of());
}
