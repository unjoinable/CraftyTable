package io.github.unjoinable.craftytable.recipe.impl;

import io.github.unjoinable.craftytable.recipe.traits.FastRecipe;
import io.github.unjoinable.craftytable.recipe.RecipeResult;
import io.github.unjoinable.craftytable.utils.CraftingGrid;

/**
 * High-performance shapeless crafting recipe implementation that ignores material
 * placement and only considers the types and quantities of materials present.
 * <p>
 * This recipe type matches any arrangement of the required materials within the
 * crafting grid, making it ideal for recipes where the spatial arrangement doesn't
 * matter (e.g., brewing ingredients, basic material combinations). The recipe uses
 * a normalized hash that produces identical values regardless of how materials are
 * arranged in the grid.
 * <p>
 * Unlike shaped recipes that require specific patterns, shapeless recipes provide
 * maximum flexibility for players while maintaining O(1) lookup performance through
 * hash-based matching.
 */
public final class ShapelessRecipe implements FastRecipe {
    private final long normalizedHash;
    private final RecipeResult result;

    /**
     * Creates a new shapeless recipe with the specified materials and result.
     * The grid's material arrangement is irrelevant for matching purposes -
     * only the types and quantities of materials matter.
     *
     * @param grid the crafting grid containing the required materials in any arrangement
     * @param result the item result produced when this recipe is crafted
     */
    public ShapelessRecipe(CraftingGrid grid, RecipeResult result) {
        this.normalizedHash = grid.normalizedHash();
        this.result = result;
    }

    /**
     * Encodes this shapeless recipe into a single normalized hash value for fast lookup.
     * <p>
     * This method generates a hash that is independent of material placement within
     * the crafting grid. The same materials in different positions will always produce
     * the same hash value, enabling efficient recipe matching in hash-based lookup
     * systems with O(1) performance.
     * <p>
     * The normalization process sorts materials by their ID values before hashing,
     * ensuring consistent results regardless of spatial arrangement. This makes
     * shapeless recipes much more efficient than shaped recipes since only one
     * hash value needs to be stored and checked.
     *
     * @return single-element array containing the normalized hash value
     */
    @Override
    public long[] encode() {
        return new long[]{normalizedHash};
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