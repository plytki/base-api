package dev.plytki.baseapi.commands.command;

import lombok.Data;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

@Data
public final class CommandExecution {

    private final BaseCommand baseCommand;
    private final CommandSender sender;
    private final Command command;
    private final String label;
    private final String[] arguments;

    private CommandExecution() {
        this.baseCommand = null;
        this.sender = null;
        this.command = null;
        this.label = null;
        this.arguments = null;
    }

    public CommandExecution(BaseCommand baseCommand, CommandSender sender, Command command, String label, String[] arguments) {
        this.baseCommand = baseCommand;
        this.sender = sender;
        this.command = command;
        this.label = label;
        this.arguments = arguments;
    }

    public void execute() {
        assert this.baseCommand != null;
        this.baseCommand.onExecute(this.sender, this.command, this.label, this.arguments);
    }

}
