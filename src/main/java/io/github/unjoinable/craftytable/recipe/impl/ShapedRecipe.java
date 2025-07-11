package io.github.unjoinable.craftytable.recipe.impl;

import io.github.unjoinable.craftytable.recipe.RecipeResult;
import io.github.unjoinable.craftytable.recipe.traits.FastRecipe;
import io.github.unjoinable.craftytable.utils.CraftingGrid;

/**
 * High-performance shaped crafting recipe implementation that supports pattern matching
 * across all possible orientations within a crafting grid.
 * <p>
 * This recipe type maintains the exact shape and material placement of the original
 * pattern while allowing the pattern to be positioned anywhere within the crafting
 * grid boundaries. All valid orientations are pre-computed and hashed for O(1)
 * recipe lookup performance.
 */
public final class ShapedRecipe implements FastRecipe {
    private final long[] encoded;
    private final RecipeResult result;

    /**
     * Creates a new shaped recipe with the specified pattern and result.
     * The grid pattern will be analyzed to generate all valid orientations
     *
     * @param grid the crafting pattern grid containing the recipe shape
     * @param result the item result produced when this recipe is crafted
     */
    public ShapedRecipe(CraftingGrid grid, RecipeResult result) {
        this.result = result;
        this.encoded = grid.getAllOrientations()
                .stream()
                .mapToLong(CraftingGrid::hash)
                .toArray();
    }

    /**
     * Encodes all possible orientations of this recipe into hash values for fast lookup.
     * <p>
     * This method generates every valid placement of the recipe pattern within the
     * crafting grid bounds and computes a hash for each orientation. The resulting
     * hash array can be used in hash-based recipe matching systems for O(1) lookup
     * performance instead of iterating through all recipes.
     * <p>
     * For example, a 2x1 pattern in a 3x3 grid would generate 6 orientations:
     * - 3 horizontal positions × 2 vertical positions = 6 total orientations
     *
     * @return array of hash values representing all valid recipe orientations
     */
    @Override
    public long[] encode() {
        return encoded;
    }

    /**
     * Returns the result item produced when this recipe is successfully crafted.
     *
     * @return the crafting result containing the output item and metadata
     */
    @Override
    public RecipeResult result() {
        return result;
    }
}