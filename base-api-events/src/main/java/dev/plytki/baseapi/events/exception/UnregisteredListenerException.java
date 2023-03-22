package dev.plytki.baseapi.events.exception;

public class UnregisteredListenerException extends Exception {

    public UnregisteredListenerException(String message) {
        super(message);
    }

}