package io.github.unjoinable.craftytable.command;

import net.minestom.server.command.builder.Command;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;

import static io.github.unjoinable.craftytable.listener.InventoryClickListener.IS_CRAFTING_TABLE;

public class TestCommand extends Command {
    public TestCommand() {
        super("test");

        addSyntax((sender, _) -> {
            var inv = new Inventory(InventoryType.CRAFTING, "Table!");
            inv.setTag(IS_CRAFTING_TABLE, true);
            ((Player) sender).openInventory(inv);
            ((Player) sender).setGameMode(GameMode.CREATIVE);
        });
    }
}
