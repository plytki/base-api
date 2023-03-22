package dev.plytki.baseapi.plugin;

import dev.plytki.baseapi.commands.CommandRegistry;
import dev.plytki.baseapi.commands.exception.FailedCommandRegistration;
import dev.plytki.baseapi.inventories.manager.InventoryRegistry;
import dev.plytki.baseapi.plugin.test.TestCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BasePlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        InventoryRegistry inventoryRegistry = new InventoryRegistry(this);
        CommandRegistry commandRegistry = new CommandRegistry(this);
        try {
            commandRegistry.register(new TestCommand("test", "This is a test command", "<command> to cos"));
        } catch (FailedCommandRegistration e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
