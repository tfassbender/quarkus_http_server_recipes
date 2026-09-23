package net.tfassbender.service;

import java.util.List;

/**
 * @param score              number of distinct searched ingredients found in the recipe
 * @param matchedIngredients the searched ingredients that were found, in query order
 */
public record IngredientSearchResult(String name, String title, int score, List<String> matchedIngredients) {}
