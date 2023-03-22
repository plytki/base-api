package dev.plytki.baseapi.commands.exception;

public class FailedCommandRegistration extends Exception {

    public FailedCommandRegistration(String message) {
        super(message);
    }

}
