package dev.plytki.baseapi.plugin;

import dev.plytki.baseapi.commands.command.BaseCommand;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.TabCompleter;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.logging.Level;

public interface IBasePlugin {

    void onPluginStart();
    void onPluginStop();
    void onPluginLoad();

    void registerCommand(String command, CommandExecutor executor, TabCompleter completer);
    void registerCommand(String command, CommandExecutor executor);


    void register(Listener listener);
    void register(BaseCommand command);
    void registerWithTabCompleter(BaseCommand command);

    void runTaskLaterAsync(BukkitRunnable runnable, long delay);
    void runTaskTimerAsync(BukkitRunnable runnable, long delay, long period);

    void broadcastToPermission(String message, String permission);

    void log(Level level, String message);
    void logInfo(String message);
    void logWarning(String message);
    void logSevere(String message);

}
