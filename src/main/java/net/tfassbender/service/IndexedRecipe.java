package net.tfassbender.service;

import java.util.Set;

/**
 * @param ingredientWords lowercase words of the recipe's "## Zutaten" sections, used by the ingredient search
 */
record IndexedRecipe(RecipeSummary summary, String html, Set<String> ingredientWords) {

    String name() {
        return summary.name();
    }
}
