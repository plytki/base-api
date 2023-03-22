package dev.plytki.baseapi.commands.command;

import lombok.Data;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.util.LinkedList;

@Data
public abstract class SubCommand implements Executable, ICommand {

    private final Command command;
    private final String name;
    private final String description;
    private final LinkedList<String> arguments = new LinkedList<>();

    protected SubCommand(Command command, String name, String description) {
        this.command = command;
        this.name = name;
        this.description = description;
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage("§7-=-=-=-=-=-=- §d" + this.name.toUpperCase().charAt(0) + this.name.substring(1).toLowerCase() + " §7-=-=-=-=-=-=-");
        sender.sendMessage(" §7- /" + this.command.getLabel() + " §d" + this.name + " §7- §f" + this.description);
    }

}