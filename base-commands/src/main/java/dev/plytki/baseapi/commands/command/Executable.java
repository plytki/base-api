package dev.plytki.baseapi.commands.command;

import org.bukkit.command.CommandSender;

public interface Executable {

    void printHelp(CommandSender sender);
    void execute(ExecutionParameters parameters);

}