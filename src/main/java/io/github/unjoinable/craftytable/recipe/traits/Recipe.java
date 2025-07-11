package io.github.unjoinable.craftytable.recipe.traits;

import io.github.unjoinable.craftytable.recipe.RecipeResult;

/**
 * Base interface for all crafting recipes in the system.
 * <p>
 * This interface defines the minimal contract that all recipe implementations must fulfill.
 * It serves as the foundation for the recipe hierarchy, providing a common abstraction
 * for accessing recipe outputs regardless of the specific recipe type or matching logic.
 * <p>
 * Implementations of this interface handle the specifics of pattern matching, ingredient
 * validation, and other recipe-specific behavior, while this interface ensures consistent
 * access to the crafting result across all recipe types.
 */
public interface Recipe {

    /**
     * Returns the result produced when this recipe is successfully crafted.
     * <p>
     * This method provides access to the output item, quantity, and any additional
     * metadata associated with the crafting result. The result is independent of
     * the recipe's matching logic and represents what the player receives upon
     * successful completion of the recipe.
     *
     * @return the crafting result containing the output item and metadata
     */
    RecipeResult result();

}