package dev.plytki.baseapi.plugin;

import dev.plytki.baseapi.plugin.test.ExampleCommand;
import dev.plytki.baseapi.redis.RedisClient;
import dev.plytki.baseapi.redis.RedisMessageSystem;
import dev.plytki.baseapi.redis.exception.ChannelAlreadyRegisteredException;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;
import org.bukkit.event.world.ChunkUnloadEvent;
import org.bukkit.inventory.ItemStack;
import org.redisson.api.RMap;
import org.redisson.codec.MsgPackJacksonCodec;
import org.redisson.config.Config;

import java.util.UUID;
import java.util.logging.Level;

public final class TestPlugin extends BasePlugin implements Listener {

    private static TestPlugin plugin;

    @Override
    public void onPluginStart() {
        // Plugin startup logic
        plugin = this;

//        register(this);
        register(new ExampleCommand());

        {
            String host = "192.168.0.99";
            int port = 6379;

            Config config = new Config();
            config.setCodec(new MsgPackJacksonCodec());
            config.useSingleServer()
                    .setAddress("redis://" + host + ":" + port)
                    .setPassword("o54ljc9ZBtjQZIxtgmwT85cKwI/YknpIUb8+v2m3qpNs4rSySYWeWvfanPyx9L/aYBOcQBTY3QeyuYfxAnPY0qGMYKFf1KFfXnBxDFQTsSvaGsYQkc5aCtlD");

            startRedisClient(config);
        }

        try {
            RedisMessageSystem<ItemStack> redisMessageSystem = getRedisClient().createMessageSystem(getGson(), ItemStack.class, "item-stack-channel");

            redisMessageSystem.subscribe((s, itemStack) -> {
                getLogger().log(Level.INFO, "received itemstack -> " + itemStack.getType().name());
            });

            redisMessageSystem.publish(new ItemStack(Material.DIRT));
        } catch (ChannelAlreadyRegisteredException e) {
            getLogger().log(Level.SEVERE, e.getMessage());
        }
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        getLogger().log(Level.INFO, "Chunk loaded!");
    }

    @EventHandler
    public void onChunkUnload(ChunkUnloadEvent event) {
        getLogger().log(Level.INFO, "Chunk unloaded!");
    }

    public static TestPlugin plugin() {
        return plugin;
    }

}
