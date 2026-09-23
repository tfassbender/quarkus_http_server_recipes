package net.tfassbender.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

class IngredientTermTest {

    @ParameterizedTest(name = "{0} matches {1}: {2}")
    @CsvSource({
            // German endings are ignored in both directions
            "tomate, tomaten, true",
            "tomaten, tomate, true",
            "ei, eier, true",
            "eier, ei, true",
            "zwiebel, zwiebeln, true",
            "kartoffel, kartoffeln, true",
            // compound words ending with the term are the same thing ...
            "zwiebel, lauchzwiebeln, true",
            "mehl, weizenmehl, true",
            "käse, reibekäse, true",
            // ... words starting with it are not
            "tomate, tomatenmark, false",
            "hack, hackfleisch, false",
            "paprika, paprikapulver, false",
            // no compound matching for short stems, which would find unrelated words
            "ei, klein, false",
            "eier, schwein, false",
            "öl, olivenöl, false",
    })
    void plainTerm(String term, String word, boolean expected) {
        assertEquals(expected, IngredientTerm.parse(term).matches(word));
    }

    @ParameterizedTest(name = "{0} matches {1}: {2}")
    @CsvSource({
            "hack*, hackfleisch, true",
            "hack*, hack, true",
            "tomate*, tomatenmark, true",
            "hack*, schinkenhack, false",
            "*öl, olivenöl, true",
            "*öl, öl, true",
            "*zwiebel, lauchzwiebeln, true",
            "*öl, ölig, false",
            "*kartoffel*, süßkartoffelpüree, true",
            "*kartoffel*, karotte, false",
    })
    void wildcardTerm(String term, String word, boolean expected) {
        assertEquals(expected, IngredientTerm.parse(term).matches(word));
    }

    @ParameterizedTest(name = "stem({0}) = {1}")
    @CsvSource({"tomaten, tomat", "tomate, tomat", "eier, ei", "ei, ei", "eiern, ei", "zwiebeln, zwiebel", "salz, salz"})
    void stem(String word, String expected) {
        assertEquals(expected, IngredientTerm.stem(word));
    }
}
