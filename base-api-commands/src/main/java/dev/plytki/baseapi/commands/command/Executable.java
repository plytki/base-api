package dev.plytki.baseapi.commands.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public interface Executable {

    void onExecute(CommandSender sender, Command command, String label, String[] args);

}