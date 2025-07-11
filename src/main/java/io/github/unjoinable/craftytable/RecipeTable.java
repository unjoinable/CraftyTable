package io.github.unjoinable.craftytable;

import io.github.unjoinable.craftytable.recipe.RecipeResult;
import io.github.unjoinable.craftytable.recipe.impl.ShapedRecipe;
import io.github.unjoinable.craftytable.recipe.impl.ShapelessRecipe;
import io.github.unjoinable.craftytable.recipe.traits.Recipe;
import io.github.unjoinable.craftytable.recipe.traits.TaggedRecipe;
import io.github.unjoinable.craftytable.utils.CraftingGrid;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * A hash-based recipe lookup table for fast shaped and shapeless recipe resolution.
 * Supports 3x3 crafting grids, handles recipe normalization and deduplication.
 */
public class RecipeTable {
    private static final Logger log = LoggerFactory.getLogger(RecipeTable.class);
    private final Long2ObjectMap<RecipeResult> shapedRecipes;
    private final Long2ObjectMap<RecipeResult> shapelessRecipes;
    private final ObjectArrayList<TaggedRecipe> taggedRecipe;

    /**
     * Constructs a recipe table from a list of recipes.
     * All valid shaped and shapeless encodings are precomputed for fast lookup.
     */
    public RecipeTable(List<Recipe> recipes) {
        this.shapedRecipes = new Long2ObjectOpenHashMap<>();
        this.shapelessRecipes = new Long2ObjectOpenHashMap<>();
        this.taggedRecipe = new ObjectArrayList<>();
        buildRecipeTable(recipes);
    }

    /**
     * Populates internal maps with encoded hashes for shaped and shapeless recipes.
     */
    private void buildRecipeTable(List<Recipe> recipes) {
        for (Recipe recipe : recipes) {
            switch (recipe) {
                case ShapedRecipe shaped -> {
                    for (long hash : shaped.encode()) {
                        shapedRecipes.put(hash, shaped.result());
                    }
                }
                case ShapelessRecipe shapeless -> shapelessRecipes.put(shapeless.encode()[0], recipe.result());
                case TaggedRecipe tagged -> taggedRecipe.add(tagged);
                default -> {} // Ignore unknown recipe types
            }
        }
    }

    /**
     * Looks up a crafting result based on the provided 3x3 grid.
     * Tries shapeless recipes first, then shaped.
     */
    public @Nullable RecipeResult lookup(CraftingGrid grid) {
        RecipeResult result;
        long shapelessHash = grid.normalizedHash();
        result = shapelessRecipes.get(shapelessHash);
        if (result != null) return result;

        long shapedHash = grid.hash();
        result = shapedRecipes.get(shapedHash);
        if (result != null) return result;

        for (TaggedRecipe recipe : taggedRecipe) {
            if (recipe.matches(grid.getData()))  return recipe.result();
        }
        return null;
    }
}