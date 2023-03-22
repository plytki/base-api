package dev.plytki.baseapi.plugin.test;

import dev.plytki.baseapi.commands.command.BaseCommand;
import dev.plytki.baseapi.commands.command.SubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand extends BaseCommand {

    public TestCommand(String name, String description, String usageMessage, String... aliases) {
        super(name, description, usageMessage, aliases);
        disallow(Sender.CONSOLE, Sender.RCON);
        assertPermission("test");
        registerSubCommand(new SubCommand(this, "invade", "Invades ukraine") {
            @Override
            public void onExecute(CommandSender sender, Command command, String label, String[] args) {
                if (args[1].equals("help")) {
                    printHelp(sender);
                    return;
                }
                sender.sendMessage("test invade");
            }
        });
    }

    @Override
    public void onExecute(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("§dTest.");
        }
    }

}