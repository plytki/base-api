package dev.plytki.baseapi.plugin.test;

import dev.plytki.baseapi.commands.command.*;
import dev.plytki.baseapi.plugin.TestPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

@CommandInfo(
        name = "example",
        description = "An example command",
        usage = "/example",
        permission = "example.use"
)
public class ExampleCommand extends BaseCommand {

    public ExampleCommand() {
        allowOnly(Sender.PLAYER);
        commandType(CommandType.SUBCOMMAND);
        registerSubCommands(
                new ExampleSubCommand(this),
                new UnloadSubCommand(this)
        );
    }

    @Override
    public void printHelp(CommandSender sender) {
        TestPlugin.plugin().logInfo("printed help");
    }

    @Override
    public void execute(Execution params) {
        if (params.isEmpty()) {
            new BukkitRunnable() {
                @Override
                public void run() {
                    if (location.isChunkLoaded()) {
                        System.out.println("Chunk is loaded");
                    } else {
                        System.out.println("Chunk is not loaded");
                    }
                    location.getWorld().spawnEntity(location, EntityType.ZOMBIE_HORSE);
                    System.out.println("spawned entity");
                }
            }.runTaskLater(TestPlugin.plugin(), 20);
//            TestPlugin.plugin().logInfo("print help");
        } else {
            TestPlugin.plugin().logInfo("handle non-registered subcommands");
        }
    }

    @SubCommandInfo(
            name = "spawn",
            permission = "example.spawn.use",
            allowed = {Sender.CONSOLE, Sender.PLAYER}
    )
    private static class ExampleSubCommand extends BaseSubCommand {

        public ExampleSubCommand(BaseCommand baseCommand) {
            super(baseCommand);
        }

        @Override
        public void printHelp(CommandSender sender) {
            TestPlugin.plugin().logInfo("printed help for subcommand");
        }

        @Override
        public void execute(Execution params) {
            CommandSender sender = params.getSender();
            Player player = params.getPlayer(0);
            if (player == null || !player.isOnline()) {
                sender.sendMessage(String.format("Player %s is offline!", params.getString(0)));
            } else {
                sender.sendMessage(String.format("Player %s is online!", player.getName()));
                World world = Bukkit.getWorlds().get(0);
                player.teleport(world.getSpawnLocation());
            }
            sender.sendMessage("Sub-command executed by: " + sender.getName());
        }

    }

    public static Location location;

    @SubCommandInfo(
            name = "set",
            permission = "example.set.use",
            allowed = {Sender.PLAYER}
    )
    private static class UnloadSubCommand extends BaseSubCommand {

        public UnloadSubCommand(BaseCommand baseCommand) {
            super(baseCommand);
        }

        @Override
        public void printHelp(CommandSender sender) {
            TestPlugin.plugin().logInfo("printed help for set");
        }

        @Override
        public void execute(Execution params) {
            CommandSender sender = params.getSender();
            Player player = (Player) sender;
            location = player.getLocation();
            sender.sendMessage("Sub-command executed by: " + sender.getName());
        }

    }

}