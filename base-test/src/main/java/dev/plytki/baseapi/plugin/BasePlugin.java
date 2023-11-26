package dev.plytki.baseapi.plugin;

import dev.plytki.baseapi.commands.CommandRegistry;
import dev.plytki.baseapi.commands.exception.FailedCommandRegistrationException;
import dev.plytki.baseapi.plugin.test.ExampleCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class BasePlugin extends JavaPlugin {

    private static BasePlugin plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        CommandRegistry commandRegistry = new CommandRegistry(this);
        try {
            commandRegistry.register(new ExampleCommand());
        } catch (FailedCommandRegistrationException e) {
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
