package io.github.unjoinable.craftytable.listener;

import io.github.unjoinable.craftytable.CraftyTable;
import io.github.unjoinable.craftytable.recipe.RecipeResult;
import io.github.unjoinable.craftytable.utils.CraftingGrid;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.inventory.InventoryClickEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public class InventoryClickListener implements EventListener<InventoryClickEvent> {
    public static final Tag<Boolean> IS_CRAFTING_TABLE = Tag.Boolean("is_crafting_table");

    @Override
    public @NotNull Class<InventoryClickEvent> eventType() {
        return InventoryClickEvent.class;
    }

    @Override
    public @NotNull Result run(@NotNull InventoryClickEvent event) {
        var inv = event.getInventory();
        if (!inv.hasTag(IS_CRAFTING_TABLE)) return Result.INVALID;

        Material[] matArray =  new Material[9];
        for (int i = 1; i < 10; i++) {
            var material = inv.getItemStack(i).material();
            if (material == Material.AIR) material = null;
            matArray[i-1] = material;
        }


        CraftingGrid grid = new CraftingGrid(3, matArray);
        RecipeResult result = CraftyTable.RECIPE_TABLE.lookup(grid);

        if (result == null) {
            inv.setItemStack(0, ItemStack.AIR);
            inv.update();
            return Result.INVALID;
        }

        inv.setItemStack(0, result.itemStack());
        inv.update();
        return Result.SUCCESS;
    }
}
