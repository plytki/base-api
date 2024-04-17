package dev.plytki.baseapi.plugin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.plytki.baseapi.commands.CommandRegistry;
import dev.plytki.baseapi.commands.command.BaseCommand;
import dev.plytki.baseapi.commands.exception.FailedCommandRegistrationException;
import dev.plytki.baseapi.inventories.manager.InventoryRegistry;
import dev.plytki.baseapi.redis.RedisClient;
import lombok.Getter;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.scheduler.BukkitRunnable;
import org.redisson.config.Config;

import java.util.logging.Level;

@Getter
public abstract class BasePlugin extends JavaPlugin implements IBasePlugin {

    private BasePlugin instance;
    private Gson gson;
    private CommandRegistry commandRegistry;
    private InventoryRegistry inventoryRegistry;
    private PluginUpdater pluginUpdater;
    private RedisClient redisClient;

    @Override
    public final void onEnable() {
        instance = this;
        GsonBuilder gsonBuilder = new GsonBuilder();
        prepareGson(gsonBuilder);
        gson = gsonBuilder.create();
        commandRegistry = new CommandRegistry(this);
        inventoryRegistry = new InventoryRegistry(this);

        onPluginStart();
        getLogger().info(this.getName() + " has been enabled!");
    }

    @Override
    public final void onDisable() {
        inventoryRegistry.destroyAllLiveInventories();
        onPluginStop();
        getLogger().info(this.getName() + " has been disabled.");
    }

    @Override
    public final void onLoad() {
        onPluginLoad();
    }

    public void prepareGson(GsonBuilder gsonBuilder) {};

    public abstract void onPluginStart();
    public void onPluginStop() {}
    public void onPluginLoad() {}

    protected final void startRedisClient(Config config) {
        if (redisClient != null) {
            redisClient.getRedisson().shutdown();
            redisClient = null;
        }
        redisClient = new RedisClient(config);
    }

    protected final void enablePluginUpdater() {
        if (pluginUpdater != null) {
            pluginUpdater.getTask().cancel();
            pluginUpdater = null;
        }
        pluginUpdater = new PluginUpdater(this);
    }

    public void register(Listener listener) {
        this.getServer().getPluginManager().registerEvents(listener, this);
    }

    @Override
    public void register(BaseCommand command) {
        try {
            this.commandRegistry.register(command);
        } catch (FailedCommandRegistrationException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void registerWithTabCompleter(BaseCommand command) {
        try {
            this.commandRegistry.register(command, command);
        } catch (FailedCommandRegistrationException e) {
            throw new RuntimeException(e);
        }
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

}