package dev.plytki.baseapi.redis;

public interface RedisSubscription<T> {

    void subscribe(String channelId, T source);
    String action();

}