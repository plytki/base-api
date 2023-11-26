package dev.plytki.baseapi.plugin;

import dev.plytki.baseapi.commands.CommandRegistry;
import dev.plytki.baseapi.inventories.manager.InventoryRegistry;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public class BasePlugin extends JavaPlugin {

    private CommandRegistry commandRegistry;
    private InventoryRegistry inventoryRegistry;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getLogger().info(this.getName() + " has been enabled!");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
        getLogger().info(this.getName() + " has been disabled.");
    }

    /**
     * Registers a command executor and tab completer for a given command.
     *
     * @param command The command for which the executor and tab completer will be registered.
     * @param executor The executor to handle command logic.
     * @param completer The tab completer to handle tab completion.
     */
    public void registerCommand(String command, CommandExecutor executor, TabCompleter completer) {
        this.getCommand(command).setExecutor(executor);
        if (completer != null) {
            this.getCommand(command).setTabCompleter(completer);
        }
    }

    /**
     * Registers a command executor for a given command.
     *
     * @param command The command for which the executor will be registered.
     * @param executor The executor to handle command logic.
     */
    public void registerCommand(String command, CommandExecutor executor) {
        registerCommand(command, executor, null);
    }

    /**
     * Runs a task asynchronously after a certain number of server ticks.
     *
     * @param runnable The task to run asynchronously.
     * @param delay The delay in server ticks before the task will be executed.
     */
    public void runTaskLaterAsync(BukkitRunnable runnable, long delay) {
        runnable.runTaskLaterAsynchronously(this, delay);
    }

    /**
     * Runs a repeated task asynchronously with an initial delay and a period.
     *
     * @param runnable The task to run asynchronously.
     * @param delay The delay in server ticks before the task will be executed for the first time.
     * @param period The period in server ticks between consecutive executions.
     */
    public void runTaskTimerAsync(BukkitRunnable runnable, long delay, long period) {
        runnable.runTaskTimerAsynchronously(this, delay, period);
    }

    /**
     * Sends a message to all players with a specific permission.
     *
     * @param message The message to send.
     * @param permission The permission required to receive the message.
     */
    public void broadcastToPermission(String message, String permission) {
        this.getServer().getOnlinePlayers().stream()
                .filter(p -> p.hasPermission(permission))
                .forEach(p -> p.sendMessage(message));
    }

    /**
     * Logs a message at a specific log level.
     *
     * @param level The log level at which to log the message.
     * @param message The message to log.
     */
    public void log(Level level, String message) {
        getLogger().log(level, message);
    }

    /**
     * Logs an informational message.
     *
     * @param message The message to log as informational.
     */
    public void logInfo(String message) {
        log(Level.INFO, message);
    }

    /**
     * Logs a warning message.
     *
     * @param message The message to log as a warning.
     */
    public void logWarning(String message) {
        log(Level.WARNING, message);
    }

    /**
     * Logs a severe error message.
     *
     * @param message The message to log as a severe error.
     */
    public void logSevere(String message) {
        log(Level.SEVERE, message);
    }

    /**
     * Gets the CommandRegistry instance.
     *
     * @return The instance of CommandRegistry associated with this plugin.
     */
    public CommandRegistry getCommandRegistry() {
        if (commandRegistry == null) {
            commandRegistry = new CommandRegistry(this);
        }
        return commandRegistry;
    }

    /**
     * Gets the InventoryRegistry instance.
     *
     * @return The instance of InventoryRegistry associated with this plugin.
     */
    public InventoryRegistry getInventoryRegistry() {
        if (inventoryRegistry == null) {
            inventoryRegistry = new InventoryRegistry(this);
        }
        return inventoryRegistry;
    }

}