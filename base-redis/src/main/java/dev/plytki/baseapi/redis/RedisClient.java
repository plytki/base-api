package dev.plytki.baseapi.redis;

import com.google.gson.Gson;
import dev.plytki.baseapi.redis.exception.ChannelNotFoundException;
import dev.plytki.baseapi.redis.exception.ChannelAlreadyRegisteredException;
import lombok.Getter;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.HashMap;
import java.util.Map;

@Getter
public class RedisClient {

    private final RedissonClient redisson;
    private final Map<String, RedisMessageSystem<?>> registeredMessageSystems;

    private RedisClient(RedissonClient redisson) {
        this.redisson = redisson;
        this.registeredMessageSystems = new HashMap<>();
    }

    public RedisClient(Config config) {
        this.redisson = Redisson.create(config);
        this.registeredMessageSystems = new HashMap<>();
    }

    public <T> RedisMessageSystem<T> createMessageSystem(Gson gson, Class<T> transferred, String channelId) throws ChannelAlreadyRegisteredException {
        RedisMessageSystem<T> tRedisMessageSystem = new RedisMessageSystem<>(gson, this, transferred, channelId);
        if (registeredMessageSystems.containsKey(channelId)) {
            throw new ChannelAlreadyRegisteredException();
        }
        registeredMessageSystems.putIfAbsent(channelId, tRedisMessageSystem);
        return tRedisMessageSystem;
    }

    public void destroyMessageSystem(String channelId) throws ChannelNotFoundException {
        if (registeredMessageSystems.containsKey(channelId)) {
            RedisMessageSystem<?> remove = registeredMessageSystems.remove(channelId);
            remove.getExecutor().shutdown();
        } else throw new ChannelNotFoundException();
    }

}
