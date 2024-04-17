package dev.plytki.baseapi.redis.exception;

public class ChannelNotFoundException extends Exception {

    public ChannelNotFoundException() {
        super("Channel not found.");
    }

    public ChannelNotFoundException(String message) {
        super(message);
    }
}
