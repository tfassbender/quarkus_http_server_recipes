package net.tfassbender.service;

import java.util.List;
import java.util.Set;

/**
 * A single searched ingredient and how it matches the words of a recipe's ingredient section:
 * <ul>
 *     <li>{@code tomate} - the same word ignoring German endings (Tomaten), or a compound word ending with it
 *     (Lauchzwiebeln for zwiebel). German compounds name the thing in their last part, so mehl finds Weizenmehl,
 *     but tomate does not find Tomatenmark.</li>
 *     <li>{@code hack*} - words starting with the term (Hackfleisch)</li>
 *     <li>{@code *öl} - words ending with the term (Olivenöl), also for short terms</li>
 *     <li>{@code *hack*} - words containing the term</li>
 * </ul>
 *
 * @param text the term as entered (lowercase), shown as matched ingredient
 * @param core the term without wildcards
 * @param stem the core without German endings
 */
record IngredientTerm(String text, String core, String stem, boolean leadingWildcard, boolean trailingWildcard) {

    static final String WILDCARD = "*";

    // longest first, so "eiern" loses "ern" instead of just "n"
    private static final List<String> ENDINGS = List.of("ern", "en", "er", "es", "e", "n", "s");
    private static final int MIN_STEM_LENGTH = 2;
    // shorter stems would make compound matching find unrelated words, e.g. "ei" in "klein" or "Schwein"
    private static final int MIN_COMPOUND_STEM_LENGTH = 3;

    /**
     * @param term a lowercase search term that contains more than just wildcards
     */
    static IngredientTerm parse(String term) {
        boolean leadingWildcard = term.startsWith(WILDCARD);
        boolean trailingWildcard = term.endsWith(WILDCARD);
        String core = term.replaceAll("^\\*+|\\*+$", "");
        return new IngredientTerm(term, core, stem(core), leadingWildcard, trailingWildcard);
    }

    boolean matchesAny(Set<String> words) {
        return words.stream().anyMatch(this::matches);
    }

    boolean matches(String word) {
        if (leadingWildcard && trailingWildcard) {
            return word.contains(core);
        }
        if (trailingWildcard) {
            return word.startsWith(core);
        }
        String wordStem = stem(word);
        if (leadingWildcard) {
            return wordStem.endsWith(stem);
        }
        return wordStem.equals(stem) || (stem.length() >= MIN_COMPOUND_STEM_LENGTH && wordStem.endsWith(stem));
    }

    /**
     * Very light German stemming: strips one common plural/inflection ending, so "tomate" and "tomaten" (or "ei"
     * and "eier") share the same stem. Applied to search terms and recipe words alike, so both sides stay comparable.
     */
    static String stem(String word) {
        for (String ending : ENDINGS) {
            if (word.endsWith(ending) && word.length() - ending.length() >= MIN_STEM_LENGTH) {
                return word.substring(0, word.length() - ending.length());
            }
        }
        return word;
    }
}
