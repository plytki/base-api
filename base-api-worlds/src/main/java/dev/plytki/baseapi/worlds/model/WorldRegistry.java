package dev.plytki.baseapi.worlds.model;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import dev.plytki.baseapi.worlds.model.BaseWorld;
import lombok.Data;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.event.world.WorldInitEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.lang.reflect.Type;
import java.util.*;

@Data
public class WorldRegistry implements Listener {

    private final Plugin plugin;
    private final Set<BaseWorld> registeredWorlds;
    private transient File configFile;

    public WorldRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.registeredWorlds = new HashSet<>();
        for (World world : this.plugin.getServer().getWorlds()) {
            BaseWorld baseWorld = createBaseWorld(world);
            baseWorld.setLoaded(true);
        }
    }

    public WorldRegistry(Plugin plugin, File worldsConfigFile) {
        this.plugin = plugin;
        this.registeredWorlds = new HashSet<>();
        this.configFile = worldsConfigFile;
        try {
            load(this.configFile);
        } catch (FileNotFoundException ignored) {}
        for (World world : this.plugin.getServer().getWorlds()) {
            BaseWorld baseWorld = createBaseWorld(world);
            baseWorld.setLoaded(true);
        }
    }

    public void registerListener() {
        this.plugin.getServer().getPluginManager().registerEvents(this, this.plugin);
    }

    private void registerWorld(BaseWorld world) {
        this.registeredWorlds.add(world);
    }

    public BaseWorld createBaseWorld(String name) {
        Optional<BaseWorld> world = getWorld(name);
        if (world.isPresent())
            return world.get();
        BaseWorld baseWorld = new BaseWorld(name);
        registerWorld(baseWorld);
        return baseWorld;
    }

    public BaseWorld createBaseWorld(World world) {
        return createBaseWorld(world.getName());
    }

    public Optional<BaseWorld> getWorld(String name) {
        return this.registeredWorlds.stream().filter(baseWorld -> baseWorld.getName().equals(name)).findFirst();
    }

    public void save(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Type setType = new TypeToken<Set<BaseWorld>>(){}.getType();
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write(gson.toJson(this.registeredWorlds, setType));
        fileWriter.close();
    }

    public void load(File file) throws FileNotFoundException {
        Type setType = new TypeToken<Set<BaseWorld>>(){}.getType();
        JsonReader json = new JsonReader(new FileReader(file));
        JsonElement parse = new JsonParser().parse(json);
        Gson gson = new Gson();
        Set<BaseWorld> worlds = gson.fromJson(parse, setType);
        for (BaseWorld world : worlds) {
            world.load(world1 -> {
                Optional<BaseWorld> world2 = getWorld(world1.getName());
                if (!world2.isPresent()) {
                    this.registeredWorlds.add(world);
                }
            });
        }
    }

    @EventHandler
    private void onPlayerTeleport(PlayerTeleportEvent event) {
        World toWorld = event.getTo().getWorld();
        if (!event.getFrom().getWorld().getName().equals(toWorld.getName())) {
            getWorld(toWorld.getName()).ifPresent(baseWorld -> {
                if (baseWorld.getSpawnPosition() != null) {
                    event.setTo(baseWorld.getSpawnPosition().toLocation());
                }
            });
        } else {
            if (event.getTo().equals(event.getTo().getWorld().getSpawnLocation())) {
                getWorld(toWorld.getName()).ifPresent(baseWorld -> {
                    if (baseWorld.getSpawnPosition() != null) {
                        event.setTo(baseWorld.getSpawnPosition().toLocation());
                    }
                });
            }
        }
    }

}