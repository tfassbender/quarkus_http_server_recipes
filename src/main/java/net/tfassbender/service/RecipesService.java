package net.tfassbender.service;

import jakarta.enterprise.context.ApplicationScoped;
import net.tfassbender.config.RecipesConfig;
import net.tfassbender.markdown.RecipesMarkdownFormatter;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@ApplicationScoped
public class RecipesService {

    private static final String EXTENSION = ".txt";

    private final Path recipesPath;
    private final RecipesMarkdownFormatter formatter;

    // Swapped atomically after a successful rebuild, so readers always see a consistent snapshot.
    private volatile RecipeIndex index = RecipeIndex.EMPTY;

    public RecipesService(RecipesConfig config, RecipesMarkdownFormatter formatter) {
        this.recipesPath = Path.of(config.path());
        this.formatter = formatter;
    }

    public List<RecipeSummary> listRecipes() {
        return index.summaries();
    }

    public String getRecipeHtml(String name) {
        // Lookups only hit names from the index, so path traversal via the name is impossible.
        return index.html(name).orElseThrow(() -> new RecipeNotFoundException(name));
    }

    public IngredientSearchResponse searchByIngredients(String query) {
        return IngredientSearch.search(index.recipes(), IngredientSearch.Query.parse(query));
    }

    /**
     * Re-reads all recipe files. On failure the previous index stays in place.
     */
    public void rebuildIndex() throws IOException {
        if (!Files.isDirectory(recipesPath)) {
            throw new IOException("Invalid recipes path: " + recipesPath.toAbsolutePath());
        }

        List<Path> files;
        try (Stream<Path> stream = Files.list(recipesPath)) {
            files = stream
                    .filter(Files::isRegularFile)
                    .filter(p -> p.getFileName().toString().toLowerCase().endsWith(EXTENSION))
                    .sorted(Comparator.comparing(Path::getFileName))
                    .toList();
        }

        List<IndexedRecipe> recipes = new ArrayList<>();
        for (Path file : files) {
            String name = stripExtension(file.getFileName().toString());
            RecipeParser.ParsedRecipe parsed = RecipeParser.parse(Files.readString(file));
            RecipeSummary summary = new RecipeSummary(name, parsed.title(), parsed.tags());
            recipes.add(new IndexedRecipe(summary, formatter.toHtml(parsed.markdown()),
                    IngredientSearch.ingredientWords(parsed.ingredients())));
        }
        index = RecipeIndex.of(recipes);
    }

    private static String stripExtension(String filename) {
        return filename.substring(0, filename.length() - EXTENSION.length());
    }
}
