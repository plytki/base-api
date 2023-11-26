package dev.plytki.baseapi.plugin.test;

import dev.plytki.baseapi.commands.command.*;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@CommandInfo(
        name = "example",
        description = "An example command",
        usage = "/example",
        permission = "example.use"
)
public class ExampleCommand extends BaseCommand {

    public ExampleCommand() {
        registerSubCommands(
                new ExampleSubCommand(this)
        );
    }

    @Override
    public void printHelp(CommandSender sender) {
        System.out.println("printed help");
    }

    @Override
    public void execute(ExecutionParameters params) {
        if (params.isEmpty()) {
            System.out.println("print help");
        } else {
            System.out.println("handle subcommands");
        }
    }


    @SubCommandInfo(
            name = "example",
            permission = "example.example.use",
            allowed = {Sender.CONSOLE, Sender.PLAYER}
    )
    private static class ExampleSubCommand extends BaseSubCommand {

        public ExampleSubCommand(BaseCommand baseCommand) {
            super(baseCommand);
        }

        @Override
        public void printHelp(CommandSender sender) {
            System.out.println("printed help for subcommand");
        }

        @Override
        public void execute(ExecutionParameters params) {
            CommandSender sender = params.getSender();
            Player player = params.getPlayer(0);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(String.format("Player %s is offline!", params.getString(0)));
            } else {
                sender.sendMessage(String.format("Player %s is online!", player.getName()));
            }
            sender.sendMessage("Sub-command executed by: " + sender.getName());
        }

    }

}