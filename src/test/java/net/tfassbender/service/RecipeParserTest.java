package net.tfassbender.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecipeParserTest {

    @Test
    void parse_extractsHashtags() {
        assertEquals(List.of("pasta", "vegetarisch"), RecipeParser.parse("<!--tags: #pasta #vegetarisch-->").tags());
        assertEquals(List.of("pasta"), RecipeParser.parse("<!--  tags:   #pasta   -->  ").tags());
        assertEquals(List.of("ok"), RecipeParser.parse("<!--tags: nohash # #ok-->").tags());
    }

    @Test
    void parse_removesOnlyMetadataLines() {
        assertEquals("# Title\n", RecipeParser.parse("<!--tags: #a-->\n# Title\n").markdown());
        assertEquals("# Title\r\n", RecipeParser.parse("<!--tags: #a-->\r\n# Title\r\n").markdown());
        assertEquals("", RecipeParser.parse("<!--tags: #a-->").markdown());
        assertEquals("# Title\n", RecipeParser.parse("# Title\n").markdown());
    }

    @Test
    void parse_readsImagesInAnyOrderWithTags() {
        RecipeParser.ParsedRecipe parsed = RecipeParser.parse(
                "<!-- image: pizza/oben.jpg -->\n<!--tags: #a-->\n<!-- image: pizza.png -->\n# Pizza\n");

        assertEquals(List.of(new RecipeImage("pizza/oben.jpg", null), new RecipeImage("pizza.png", null)), parsed.images());
        assertEquals(List.of("a"), parsed.tags());
        assertEquals("# Pizza\n", parsed.markdown());
    }

    @Test
    void parse_readsImageCaptions() {
        RecipeParser.ParsedRecipe parsed = RecipeParser.parse(
                "<!-- image: pizza.jpg |  Frisch aus dem Ofen | heiß -->\n<!-- image: teig.jpg | -->\n<!-- image: | ohne Bild -->\n");

        assertEquals(List.of(new RecipeImage("pizza.jpg", "Frisch aus dem Ofen | heiß"), new RecipeImage("teig.jpg", null)),
                parsed.images());
    }

    @Test
    void parse_readsMetadataOnlyAtTheTop() {
        RecipeParser.ParsedRecipe parsed = RecipeParser.parse("# Pizza\n<!-- image: pizza.png -->\n");

        assertEquals(List.of(), parsed.images());
        assertEquals("# Pizza\n<!-- image: pizza.png -->\n", parsed.markdown());
    }

    @Test
    void parse_readsTitleAndTags() {
        RecipeParser.ParsedRecipe parsed = RecipeParser.parse("<!--tags: #a #b-->\n\n#  Pizza  \n## Zutaten\n");

        assertEquals("Pizza", parsed.title());
        assertEquals(List.of("a", "b"), parsed.tags());
        assertEquals("\n#  Pizza  \n## Zutaten\n", parsed.markdown());
    }

    @Test
    void extractIngredients_readsOnlyZutatenSections() {
        String content = """
                # Pizza
                Ei im Intro
                ## Zutaten
                | 150g | Mehl |
                ### Belag
                Käse
                ## Zubereitung
                Ei aufschlagen
                ## zutaten:
                Salz
                """;

        assertEquals("| 150g | Mehl |\n### Belag\nKäse\nSalz\n", RecipeParser.extractIngredients(content.lines().toList()));
    }

    @Test
    void extractIngredients_withoutSection_isEmpty() {
        assertEquals("", RecipeParser.parse("# Spare Ribs\n## Zubereitung\n500g Spare Ribs\n").ingredients());
    }

    @Test
    void parse_withoutTitle() {
        RecipeParser.ParsedRecipe parsed = RecipeParser.parse("just text\n## Sub heading\n");

        assertNull(parsed.title());
        assertEquals(List.of(), parsed.tags());
    }
}
