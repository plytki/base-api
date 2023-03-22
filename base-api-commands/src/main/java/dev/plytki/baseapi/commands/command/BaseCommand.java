package dev.plytki.baseapi.commands.command;

import dev.plytki.baseapi.commands.CommandRegistry;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class BaseCommand extends Command implements Executable, ICommand {

    @Getter
    private final Set<Sender> allowedSenders = new HashSet<>();
    private boolean disabled;
    private boolean checkOp;

    @Getter
    private final LinkedHashSet<SubCommand> subCommands = new LinkedHashSet<>();
    @Getter
    private final Set<String> assertedPermissions = new HashSet<>();

    @Getter
    @Setter
    private String notForPlayersMessage, notForConsoleMessage, notForRCONMessage,
            disabledMessage, insufficientPermissionsMessage;

    public BaseCommand(String name, String description, String usageMessage, String... aliases) {
        super(name, description == null ? "" : description, usageMessage == null ? "" : usageMessage, Arrays.asList(aliases));
        this.disabled = false;
        this.allowedSenders.addAll(CommandRegistry.getDefaultAllowedSenders());
        setupMessages();
    }

    private void setupMessages() {
        this.notForPlayersMessage = "§cCommand is not available for players!";
        this.notForConsoleMessage = "§cCommand is not available for console!";
        this.notForRCONMessage = "§cCommand is not available for RCON!";
        this.disabledMessage = "§cCommand is currently disabled!";
        this.insufficientPermissionsMessage = "§cInsufficient permissions!";
    }

    // Enable and disable commands
    public void enable() { this.disabled = false; }
    public void disable() { this.disabled = true; }

    // Assert op and permissions
    protected void assertOp() { this.checkOp = true; }
    protected void assertPermission(String permission) { this.assertedPermissions.add(permission); }
    protected void assertPermissions(String... permissions) { this.assertedPermissions.addAll(Arrays.asList(permissions)); }

    // Allow and disallow senders
    protected void allow(Sender... senderTypes) {
        this.allowedSenders.clear();
        this.allowedSenders.addAll(Arrays.asList(senderTypes));
    }

    protected void disallow(Sender... senderTypes) {
        this.allowedSenders.clear();
        this.allowedSenders.addAll(Arrays.asList(Sender.values()));
        Arrays.asList(senderTypes).forEach(this.allowedSenders::remove);
    }

    protected void allowPlayer() { this.allowedSenders.add(Sender.PLAYER); }
    protected void disallowPlayer() { this.allowedSenders.remove(Sender.PLAYER); }
    protected void allowConsole() { this.allowedSenders.add(Sender.CONSOLE); }
    protected void disallowConsole() { this.allowedSenders.remove(Sender.CONSOLE); }
    protected void allowRCON() { this.allowedSenders.add(Sender.RCON); }
    protected void disallowRCON() { this.allowedSenders.remove(Sender.RCON); }

    private boolean canPlayerExec() { return this.allowedSenders.contains(Sender.PLAYER); }
    private boolean canConsoleExec() { return this.allowedSenders.contains(Sender.CONSOLE); }
    private boolean canRCONExec() { return this.allowedSenders.contains(Sender.RCON); }

    protected void registerSubCommands(SubCommand... subCommands) {
        for (SubCommand subCommand : subCommands) {
            registerSubCommand(subCommand);
        }
    }

    protected void registerSubCommand(SubCommand subCommand) {
        this.subCommands.add(subCommand);
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage(" ");
        sender.sendMessage("§7-=-=-=-=-=-=- §d" + this.getName().toUpperCase().charAt(0) + this.getName().substring(1).toLowerCase() + " §7-=-=-=-=-=-=-");
        for (SubCommand subCommand : this.subCommands) {
            StringBuilder sb = new StringBuilder();
            sb.append(" §8- §7/").append(this.getLabel()).append(" §d").append(subCommand.getName().toLowerCase()).append("§7").append(subCommand.getArguments().size() == 0 ? "" : " ");
            for (String arg : subCommand.getArguments()) {
                sb.append(arg);
            }
            sb.append(" §7- §f").append(subCommand.getDescription());
            sender.sendMessage(sb.toString());
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        CommandExecution commandExecution = new CommandExecution(this, sender, this, label, args);
        if (sender instanceof Player && !canPlayerExec()) {
            sender.sendMessage(this.notForPlayersMessage);
            return false;
        }
        if (sender instanceof
                ConsoleCommandSender && !canConsoleExec()) {
            sender.sendMessage(this.notForConsoleMessage);
            return false;
        }
        if (sender instanceof RemoteConsoleCommandSender && !canRCONExec()) {
            sender.sendMessage(this.notForRCONMessage);
            return false;
        }
        if (this.disabled) {
            sender.sendMessage(this.disabledMessage);
            return false;
        }
        if (this.checkOp && !sender.isOp()) {
            sender.sendMessage(this.insufficientPermissionsMessage);
            return false;
        }
        if (args.length > 0 && !this.subCommands.isEmpty()) {
            this.subCommands.stream().filter(subCommand -> subCommand.getName().equalsIgnoreCase(args[0])).findFirst().ifPresentOrElse(subCommand -> {
                subCommand.onExecute(sender, this, label, args);
            }, () -> {
                printHelp(sender);
            });
        } else {
            commandExecution.execute();
        }
        return true;
    }

    public enum Sender {
        PLAYER,
        CONSOLE,
        RCON
    }

}