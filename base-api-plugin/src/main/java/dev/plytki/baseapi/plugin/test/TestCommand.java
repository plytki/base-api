package dev.plytki.baseapi.plugin.test;

import dev.plytki.baseapi.commands.command.BaseCommand;
import dev.plytki.baseapi.commands.command.SubCommand;
import dev.plytki.baseapi.items.BaseItem;
import dev.plytki.baseapi.plugin.BasePlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TestCommand extends BaseCommand {

    public TestCommand(String name, String description, String usageMessage, String... aliases) {
        super(name, description, usageMessage, aliases);
        disallow(Sender.CONSOLE, Sender.RCON);
        assertPermission("test");
        registerSubCommand(new SubCommand(this, "invade", "Test subcommand") {
            @Override
            public void onExecute(CommandSender sender, Command command, String label, String[] args) {
                if (args.length > 1 && args[1].equals("help")) {
                    printHelp(sender);
                    return;
                }
                Player player = (Player) sender;
                for (BaseItem item : BasePlugin.itemRegistry.getItems()) {
                    player.sendMessage(item.toString());
                    player.getInventory().addItem(item.clone());
                }
            }
        });
    }

    @Override
    public void onExecute(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;
        if (args.length == 0) {
            player.sendMessage("§dWorking!");
        }
    }

}