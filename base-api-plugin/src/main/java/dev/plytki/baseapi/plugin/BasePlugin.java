package dev.plytki.baseapi.plugin;

import dev.plytki.baseapi.commands.CommandRegistry;
import dev.plytki.baseapi.commands.exception.FailedCommandRegistration;
import dev.plytki.baseapi.inventories.manager.InventoryRegistry;
import dev.plytki.baseapi.items.BaseItem;
import dev.plytki.baseapi.items.exception.ItemRegisteredException;
import dev.plytki.baseapi.items.manager.ItemRegistry;
import dev.plytki.baseapi.plugin.test.TestCommand;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

public final class BasePlugin extends JavaPlugin {

    private static BasePlugin plugin;

    public static ItemRegistry itemRegistry;

    @Override
    public void onEnable() {
        plugin = this;
        // Plugin startup logic
        InventoryRegistry inventoryRegistry = new InventoryRegistry(this);
        CommandRegistry commandRegistry = new CommandRegistry(this);
        itemRegistry = new ItemRegistry(this);
        try {
            itemRegistry.registerItems(
                    BaseItem.builder("test", new ItemStack(Material.ACACIA_FENCE)).build(),
                    BaseItem.builder("test2", new ItemStack(Material.OAK_BOAT)).build(),
                    BaseItem.builder("test3", "6080972b7c32da650cc86e9aad665ed8d0d48a3bdb0ba234d4fceac024c7952e").build()
            );
        } catch (ItemRegisteredException e) {
            getLogger().log(Level.SEVERE, e.getMessage());
        }
        try {
            commandRegistry.register(new TestCommand("test", "This is a test command", "/<command> invade"));
        } catch (FailedCommandRegistration e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static BasePlugin plugin() {
        return plugin;
    }

}
