package net.tfassbender.service;

import java.util.List;

/**
 * @param images images shown below the title
 */
public record RecipeSummary(String name, String title, List<String> tags, List<RecipeImage> images) {}
