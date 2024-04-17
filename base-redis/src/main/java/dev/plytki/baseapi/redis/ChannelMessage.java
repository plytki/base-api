package dev.plytki.baseapi.redis;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(force = true, access = AccessLevel.PRIVATE)
public class ChannelMessage<T> {

    private final String serverId;
    private final String key;
    private final T source;

    public ChannelMessage(String serverId, String key, T source) {
        this.serverId = serverId;
        this.key = key;
        this.source = source;
    }

    public String serverId() {
        return serverId;
    }

    public String key() {
        return key;
    }

    public T source() {
        return source;
    }

}