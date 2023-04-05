package dev.plytki.baseapi.worlds.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.util.function.Consumer;

@EqualsAndHashCode(callSuper = true)
@Data
public class BaseWorld extends BaseWorldProperties {

    private final String name;
    private final String worldFolder;
    private transient boolean loaded;

    protected BaseWorld(String name) {
        this.name = name;
        this.worldFolder = new File(this.name).getName();
    }

    protected BaseWorld(World world) {
        this.name = world.getName();
        super.setChunkGenerator(world.getGenerator());
        super.setEnvironment(world.getEnvironment());
        super.setSeed(world.getSeed());
        this.worldFolder = world.getWorldFolder().getName();
    }

    public World createWorld(WorldCreator worldCreator, Consumer<World> worldConsumer) {
        World createdWorld = worldCreator.createWorld();
        this.loaded = true;
        this.setEnvironment(worldCreator.environment());
        this.setSeed(worldCreator.seed());
        this.setChunkGenerator(worldCreator.generator());
        worldConsumer.accept(createdWorld);
        return createdWorld;
    }

    public World createWorld(Consumer<World> worldConsumer) {
        World createdWorld = getWorldCreator(this.name).createWorld();
        this.loaded = true;
        this.setEnvironment(createdWorld.getEnvironment());
        this.setSeed(createdWorld.getSeed());
        this.setChunkGenerator(createdWorld.getGenerator());
        worldConsumer.accept(createdWorld);
        return createdWorld;
    }

    public void unload(boolean save) {
        if (!this.loaded) return;
        Bukkit.unloadWorld(this.name, save);
        this.loaded = false;
    }

    public void load(Consumer<World> worldConsumer) {
        if (this.loaded) return;
        createWorld(worldConsumer);
    }

    public File getWorldFolder() {
        return new File(this.worldFolder);
    }

    public World getWorld() {
        return Bukkit.getWorld(this.name);
    }

}