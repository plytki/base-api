package dev.plytki.baseapi.plugin;

import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

@Getter
public class PluginUpdater extends BukkitRunnable {

    private final Plugin plugin;
    private final BukkitTask task;

    public PluginUpdater(Plugin plugin) {
        this.plugin = plugin;
        this.task = runTaskTimer(plugin, 20, 20);
    }

    private boolean isValidPluginJar(File pluginJar) {
        try (ZipFile zipFile = new ZipFile(pluginJar)) {
            ZipEntry pluginYml = zipFile.getEntry("plugin.yml");
            if (pluginYml == null) {
                plugin.getLogger().info("No plugin.yml found in " + pluginJar.getName());
                return false;
            }
            try (InputStream is = zipFile.getInputStream(pluginYml);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.replaceAll(" ", "");
                    if (line.startsWith("name:")) {
                        String pluginName = line.substring(5).trim();
                        if (!pluginName.equals(plugin.getName()))
                            return false;
                        plugin.getLogger().info("Plugin found: " + pluginName);
                        break;
                    }
                }
            }
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to open plugin JAR (" + pluginJar.getName() + "): " + e.getMessage());
            return false;
        }
    }

    private void reload() {
        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
            if (onlinePlayer.isOp()) {
                onlinePlayer.sendMessage("§8[§a" + plugin.getName() + "§8] §7» §eUpdated plugin file found! Reloading..");
            }
        }
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "plugman reload " + plugin.getName());
    }

    @Override
    public void run() {
        File updateFolder = new File(plugin.getDataFolder().getParentFile(), "update");
        if (!updateFolder.exists()) {
            return;
        }
        File[] updates = updateFolder.listFiles();
        if (updates == null) return;
        for (File update : updates) {
            try {
                if (update.getName().endsWith(".jar")) {
                    if (isValidPluginJar(update)) {
                        reload();
                    }
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

}