package dev.plytki.baseapi.worlds.model;

import dev.plytki.baseapi.commons.location.WorldPosition;
import lombok.Data;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.generator.ChunkGenerator;

@Data
public class BaseWorldProperties {

    private WorldPosition spawnPosition;
    private long seed;
    private World.Environment environment;
    private transient ChunkGenerator chunkGenerator;

    public BaseWorldProperties() {}

    public WorldCreator getWorldCreator(String name) {
        WorldCreator worldCreator = WorldCreator.name(name);
        if (this.seed != 0)
            worldCreator.seed(this.seed);
        if (this.environment != null)
            worldCreator.environment(this.environment);
        if (this.chunkGenerator != null)
            worldCreator.generator(this.chunkGenerator);
        return worldCreator;
    }

}