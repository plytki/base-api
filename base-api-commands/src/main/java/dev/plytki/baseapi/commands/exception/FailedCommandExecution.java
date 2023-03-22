package dev.plytki.baseapi.commands.exception;

public abstract class FailedCommandExecution extends Exception {

    public FailedCommandExecution(String message) {
        super(message);
    }

}
