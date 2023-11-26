package dev.plytki.baseapi.plugin;

import dev.plytki.baseapi.commands.exception.FailedCommandRegistrationException;
import dev.plytki.baseapi.plugin.test.ExampleCommand;

public final class TestPlugin extends BasePlugin {

    private static TestPlugin plugin;

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;
        try {
            getCommandRegistry().register(new ExampleCommand());
        } catch (FailedCommandRegistrationException e) {
            throw new RuntimeException(e);
        }
    }

    public static TestPlugin plugin() {
        return plugin;
    }

}
