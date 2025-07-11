package io.github.unjoinable.craftytable;

import io.github.unjoinable.craftytable.command.TestCommand;
import io.github.unjoinable.craftytable.listener.InventoryClickListener;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.AsyncPlayerConfigurationEvent;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.LightingChunk;
import net.minestom.server.instance.block.Block;

public class CraftyTable {
    public static final RecipeTable RECIPE_TABLE = new RecipeTable(new RecipeLoader().loadAll());

    public static void main(String[] args) {
        MinecraftServer minecraftServer = MinecraftServer.init();
        InstanceManager instanceManager = MinecraftServer.getInstanceManager();
        InstanceContainer instanceContainer = instanceManager.createInstanceContainer();
        instanceContainer.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.GRASS_BLOCK));
        instanceContainer.setChunkSupplier(LightingChunk::new);
        GlobalEventHandler globalEventHandler = MinecraftServer.getGlobalEventHandler();
        globalEventHandler.addListener(AsyncPlayerConfigurationEvent.class, event -> {
            final Player player = event.getPlayer();
            event.setSpawningInstance(instanceContainer);
           player.setRespawnPoint(new Pos(0, 42, 0));
        });
        globalEventHandler.addListener(new InventoryClickListener());
        MinecraftServer.getCommandManager().register(new TestCommand());
        minecraftServer.start("0.0.0.0", 25565);
    }
}
