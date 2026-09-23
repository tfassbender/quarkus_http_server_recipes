package net.tfassbender.service;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RecipeParserTest {

    @Test
    void parseTagLine_extractsHashtags() {
        assertEquals(List.of("pasta", "vegetarisch"), RecipeParser.parseTagLine("<!--tags: #pasta #vegetarisch-->"));
        assertEquals(List.of("pasta"), RecipeParser.parseTagLine("<!--  tags:   #pasta   -->  "));
    }

    @Test
    void parseTagLine_ignoresInvalidInput() {
        assertEquals(List.of(), RecipeParser.parseTagLine(null));
        assertEquals(List.of(), RecipeParser.parseTagLine("# Title"));
        assertEquals(List.of("ok"), RecipeParser.parseTagLine("<!--tags: nohash # #ok-->"));
    }

    @Test
    void stripTagLine_removesOnlyTagLine() {
        assertEquals("# Title\n", RecipeParser.stripTagLine("<!--tags: #a-->\n# Title\n"));
        assertEquals("# Title\r\n", RecipeParser.stripTagLine("<!--tags: #a-->\r\n# Title\r\n"));
        assertEquals("", RecipeParser.stripTagLine("<!--tags: #a-->"));
        assertEquals("# Title\n", RecipeParser.stripTagLine("# Title\n"));
    }

    @Test
    void parse_readsTitleAndTags() {
        RecipeParser.ParsedRecipe parsed = RecipeParser.parse("<!--tags: #a #b-->\n\n#  Pizza  \n## Zutaten\n");

        assertEquals("Pizza", parsed.title());
        assertEquals(List.of("a", "b"), parsed.tags());
        assertEquals("\n#  Pizza  \n## Zutaten\n", parsed.markdown());
    }

    @Test
    void parse_withoutTitle() {
        RecipeParser.ParsedRecipe parsed = RecipeParser.parse("just text\n## Sub heading\n");

        assertNull(parsed.title());
        assertEquals(List.of(), parsed.tags());
    }
}
