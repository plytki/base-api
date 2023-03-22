package dev.plytki.baseapi.commands.exception;

public class IgnoredCommandException extends Exception {

    public IgnoredCommandException() {
        super();
    }

    public IgnoredCommandException(String message) {
        super(message);
    }

}
