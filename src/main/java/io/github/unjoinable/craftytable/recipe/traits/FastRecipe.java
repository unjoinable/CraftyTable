package io.github.unjoinable.craftytable.recipe.traits;

/**
 * High-performance recipe interface that enables O(1) recipe lookup through hash-based encoding.
 * <p>
 * This interface extends the base Recipe contract with an encoding mechanism that converts
 * recipe patterns into hash values for efficient storage and retrieval. Instead of iterating
 * through all recipes to find matches, implementations can pre-compute hash values and use
 * hash-based data structures for constant-time lookups.
 * <p>
 * The encoding approach supports both shaped and shapeless recipes:
 * <ul>
 * <li><strong>Shaped recipes</strong> encode all valid orientations of a pattern</li>
 * <li><strong>Shapeless recipes</strong> encode a single normalized hash regardless of arrangement</li>
 * </ul>
 * <p>
 * This design dramatically improves crafting performance in scenarios with large numbers
 * of recipes by eliminating the need for sequential recipe matching.
 */
public interface FastRecipe extends Recipe {

    /**
     * Encodes this recipe into an array of hash values for fast lookup operations.
     * <p>
     * The returned hash array represents all possible variations of this recipe that
     * should be considered valid matches. For shaped recipes, this includes all valid
     * orientations of the pattern within the crafting grid. For shapeless recipes,
     * this typically contains a single normalized hash value.
     * <p>
     * These hash values can be used as keys in hash-based data structures (like HashMap)
     * to enable O(1) recipe lookups instead of O(n) linear searches through all recipes.
     * <p>
     * <strong>Performance Note:</strong> This method may perform computationally expensive
     * operations (like generating all orientations) and should typically be called once
     * during recipe registration rather than during each crafting operation.
     *
     * @return array of hash values representing all valid recipe variations
     */
    long[] encode();

}