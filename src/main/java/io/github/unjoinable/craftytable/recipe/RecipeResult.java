package io.github.unjoinable.craftytable.recipe;

import net.minestom.server.codec.Codec;
import net.minestom.server.codec.StructCodec;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

/**
 * Immutable record representing the result of a crafting recipe.
 * <p>
 * This record encapsulates the output item that players receive when successfully
 * crafting a recipe. It provides both direct ItemStack access and serialization
 * capabilities for persistent storage and network transmission.
 * <p>
 * The record includes a built-in codec for efficient serialization, making it suitable
 * for storing recipe data in configuration files, databases, or transmitting over
 * network protocols.
 *
 * @param itemStack the resulting item stack containing the crafted item, quantity, and metadata
 */
public record RecipeResult(ItemStack itemStack) {
    public static final Codec<RecipeResult> CODEC = StructCodec.struct(
            "count", Codec.INT, r -> r.itemStack.amount(),
            "id", Material.CODEC, r -> r.itemStack.material(),
            RecipeResult::new
    );

    /**
     * Creates a new RecipeResult with the specified count and material.
     * <p>
     * This convenience constructor automatically creates an ItemStack with the given
     * material and quantity, simplifying recipe result creation when only basic
     * item information is needed.
     *
     * @param count the number of items in the result stack
     * @param material the type of material for the result item
     */
    private RecipeResult(int count, Material material) {
        this(ItemStack.of(material, count));
    }
}