package io.github.unjoinable.craftytable.recipe.impl;

import io.github.unjoinable.craftytable.recipe.RecipeResult;
import io.github.unjoinable.craftytable.recipe.traits.TaggedRecipe;
import io.github.unjoinable.craftytable.utils.GridOrientationGenerator;
import io.github.unjoinable.craftytable.utils.Matcher;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Tag-based shaped crafting recipe implementation that supports flexible pattern matching
 * using material tags and matchers across all possible orientations.
 * <p>
 * Unlike hash-based recipes, this implementation uses {@link Matcher} objects to define
 * recipe patterns, allowing for flexible matching criteria such as material tags,
 * item groups, or custom predicates. The recipe maintains its exact shape while
 * supporting all valid orientations within the crafting grid.
 * <p>
 * All possible orientations are pre-computed during construction for optimal runtime
 * performance, avoiding the need to generate orientations during recipe matching.
 *
 * @see TaggedRecipe
 * @see Matcher
 */
public final class ShapedTaggedRecipe implements TaggedRecipe {
    private final RecipeResult result;
    private final List<Matcher[]> cachedOrientations;

    /**
     * Creates a new shaped tagged recipe with the specified pattern matchers and result.
     * <p>
     * The matchers array represents a square grid pattern where each matcher defines
     * the acceptance criteria for that position. The grid size is automatically
     * calculated as the square root of the array length.
     * <p>
     * All valid orientations (rotations and reflections) are pre-computed and cached
     * during construction to optimize runtime matching performance.
     *
     * @param matchers array of matchers defining the recipe pattern in row-major order
     * @param result the item result produced when this recipe is crafted
     * @throws IllegalArgumentException if matchers array length is not a perfect square
     */
    public ShapedTaggedRecipe(Matcher<Material>[] matchers, RecipeResult result) {
        this.result = result;
        this.cachedOrientations = GridOrientationGenerator.getAllOrientations(matchers, ((int) Math.sqrt(matchers.length)), Matcher.class);
    }

    /**
     * Checks if the provided input materials match this recipe in any valid orientation.
     * <p>
     * This method iterates through all pre-computed orientations and tests each one
     * against the input materials using the configured matchers. The first matching
     * orientation causes the method to return true immediately.
     * <p>
     * Performance optimization: Uses labeled break to exit nested loops efficiently
     * when a non-matching position is found in an orientation.
     *
     * @param inputMaterials array of materials to test against this recipe pattern
     * @return true if the input materials match any valid orientation of this recipe
     */
    @Override
    public boolean matches(@NotNull Material[] inputMaterials) {
        if (cachedOrientations.isEmpty()) return false;

        final int expectedLength = cachedOrientations.getFirst().length;
        if (inputMaterials.length != expectedLength) return false;

        for (Matcher<Material>[] orientation : cachedOrientations) {
            if (matchesOrientation(orientation, inputMaterials)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchesOrientation(Matcher<Material>[] orientation, Material[] inputMaterials) {
        for (int i = 0; i < orientation.length; i++) {
            var matcher = orientation[i];
            var inputMaterial = inputMaterials[i];

            if (matcher == null && inputMaterial == null) {
                continue; // Both null, it's a match
            }

            if (matcher == null || inputMaterial == null) {
                return false; // One is null, the other isn't
            }

            if (!matcher.matches(inputMaterial)) {
                return false; // Matcher exists but doesn't match
            }
        }
        return true;
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