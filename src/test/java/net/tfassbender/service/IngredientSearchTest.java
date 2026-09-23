package net.tfassbender.service;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class IngredientSearchTest {

    private static final IndexedRecipe BOLOGNESE = recipe("bolognese", "Bolognese", List.of("nudeln"),
            "| 250g | Spaghetti |\n| 1 Dose | Tomaten |\n| 1 | Zwiebel |\nSalz, Pfeffer, Tomaten\n");
    private static final IndexedRecipe CHILI = recipe("chilli", "Chili", List.of("Fleisch"),
            "| 1 Dose | Tomaten |\n| 2 | Zwiebel |\n| 1 | Paprika |\n");
    private static final IndexedRecipe PANCAKES = recipe("pfannekuchen", "Pfannekuchen", List.of("süß"),
            "| 2 | Eier |\n| 200g | Mehl |\n");
    private static final List<IndexedRecipe> RECIPES = List.of(BOLOGNESE, CHILI, PANCAKES);

    @Test
    void parse_splitsIngredientsAndTags() {
        IngredientSearch.Query query = IngredientSearch.Query.parse("  Tomaten, zwiebel #Nudeln tomaten # ** ");

        assertEquals(List.of("tomaten", "zwiebel"), query.ingredients());
        assertEquals(List.of("nudeln"), query.tags());
    }

    @Test
    void search_scoresDistinctIngredientsAndSortsByScore() {
        IngredientSearchResponse response = search("tomaten zwiebel paprika");

        assertEquals(3, response.ingredientCount());
        assertEquals(List.of("chilli", "bolognese"), names(response.results()));
        assertEquals(3, response.results().get(0).score());
        // "Tomaten" appears twice in the bolognese but only counts once
        assertEquals(2, response.results().get(1).score());
        assertEquals(List.of("tomaten", "zwiebel"), response.results().get(1).matchedIngredients());
        assertTrue(response.resultsMissingTags().isEmpty());
    }

    @Test
    void search_sortsEqualScoresByName() {
        assertEquals(List.of("bolognese", "chilli"), names(search("zwiebel").results()));
    }

    @Test
    void search_toleratesGermanEndings() {
        assertEquals(List.of("pfannekuchen"), names(search("ei").results()));
        assertEquals(List.of("bolognese", "chilli"), names(search("tomate").results()));
    }

    @Test
    void search_supportsWildcards() {
        IngredientSearchResponse response = search("spag* *ika");

        assertEquals(List.of("bolognese", "chilli"), names(response.results()));
        assertEquals(List.of("spag*"), response.results().get(0).matchedIngredients());
    }

    @Test
    void search_withTag_listsRecipesWithoutTagSeparately() {
        IngredientSearchResponse response = search("tomaten #nudeln");

        assertEquals(List.of("bolognese"), names(response.results()));
        assertEquals(List.of("chilli"), names(response.resultsMissingTags()));
    }

    @Test
    void search_tagsOnly_listsAllRecipesWithTheseTags() {
        IngredientSearchResponse response = search("#fleisch");

        assertEquals(0, response.ingredientCount());
        assertEquals(List.of("chilli"), names(response.results()));
        assertTrue(response.resultsMissingTags().isEmpty());
    }

    @Test
    void search_emptyQuery_returnsNothing() {
        IngredientSearchResponse response = search("   ");

        assertTrue(response.results().isEmpty());
        assertTrue(response.resultsMissingTags().isEmpty());
    }

    @Test
    void ingredientWords_splitsIntoLowercaseWords() {
        assertEquals(Set.of("1", "el", "olivenöl", "pfeffer", "salz"),
                IngredientSearch.ingredientWords("| 1 EL | Olivenöl |\nPfeffer, SALZ."));
    }

    private static IngredientSearchResponse search(String query) {
        return IngredientSearch.search(RECIPES, IngredientSearch.Query.parse(query));
    }

    private static List<String> names(List<IngredientSearchResult> results) {
        return results.stream().map(IngredientSearchResult::name).toList();
    }

    private static IndexedRecipe recipe(String name, String title, List<String> tags, String ingredients) {
        return new IndexedRecipe(new RecipeSummary(name, title, tags), "", IngredientSearch.ingredientWords(ingredients));
    }
}
