package dev.plytki.baseapi.redis.exception;

public class ChannelAlreadyRegisteredException extends Exception {

    public ChannelAlreadyRegisteredException() {
        super("Channel already registered.");
    }

    public ChannelAlreadyRegisteredException(String message) {
        super(message);
    }

}
