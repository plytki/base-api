package dev.plytki.baseapi.redis;

import com.google.gson.Gson;
import lombok.Getter;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;

import java.lang.reflect.Type;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.BiConsumer;

public class RedisMessageSystem<T> {

    @Getter
    private final ExecutorService executor;
    private final Gson gson;
    private final RedissonClient redissonClient;
    private final Type typeToken;
    private final String channelId;

    public RedisMessageSystem(Gson gson, RedisClient redisClient, Class<T> typeToken, String channelId) {
        this.executor = Executors.newCachedThreadPool();
        this.gson = gson;
        this.redissonClient = redisClient.getRedisson();
        this.typeToken = typeToken;
        this.channelId = channelId;
    }

    public void subscribe(BiConsumer<String, T> messageHandler) {
        executor.execute(() -> {
            RTopic rTopic = redissonClient.getTopic(channelId);
            rTopic.addListener(String.class, (channelId, message) -> {
                T parsedMessage = gson.fromJson(message, typeToken);
                messageHandler.accept(String.valueOf(channelId), parsedMessage);
            });
        });
    }

    public void publish(T message) {
        RTopic rTopic = redissonClient.getTopic(channelId);
        String jsonMessage = gson.toJson(message, typeToken);
        rTopic.publish(jsonMessage);
    }

}