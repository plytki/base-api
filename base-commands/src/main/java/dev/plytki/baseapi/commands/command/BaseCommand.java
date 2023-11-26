package dev.plytki.baseapi.commands.command;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public abstract class BaseCommand extends Command implements Executable, TabCompleter {

    private static final String DEFAULT_NOT_AVAILABLE = "§cCommand is not available for %s!";
    private static final String DEFAULT_DISABLED_MESSAGE = "§cCommand is currently disabled!";
    private static final String DEFAULT_PERMISSION_MESSAGE = "§cInsufficient permissions! (%s)";


    @Getter
    private final Set<Sender> allowedSenders = EnumSet.noneOf(Sender.class);
    private boolean disabled;
    private boolean requiredOp;

    private TabCompleter tabCompleter;
    @Getter
    private final Map<String, BaseSubCommand> subCommandsMap = new HashMap<>();
    @Getter
    private String permission;

    @Getter
    @Setter
    private String notForPlayersMessage,
            notForConsoleMessage,
            notForRCONMessage,
            disabledMessage,
            insufficientPermissionsMessage;

    public BaseCommand() {
        super("defaultName");
        initializeCommandFromAnnotation();
        setupMessages();
    }

    private void setupMessages() {
        this.notForPlayersMessage = String.format(DEFAULT_NOT_AVAILABLE, "players");
        this.notForConsoleMessage = String.format(DEFAULT_NOT_AVAILABLE, "console");
        this.notForRCONMessage = String.format(DEFAULT_NOT_AVAILABLE, "RCON");
        this.disabledMessage = DEFAULT_DISABLED_MESSAGE;
        this.insufficientPermissionsMessage = DEFAULT_PERMISSION_MESSAGE;
    }

    public void disabled(boolean disabled) {
        this.disabled = disabled;
    }

    protected void requiredOp(boolean required) {
        this.requiredOp = required;
    }

    protected void allow(Sender... senders) {
        this.allowedSenders.addAll(Arrays.asList(senders));
    }

    protected void disallow(Sender... senders) {
        Arrays.asList(senders).forEach(this.allowedSenders::remove);
    }

    protected void allowPlayer() { this.allowedSenders.add(Sender.PLAYER); }
    protected void disallowPlayer() { this.allowedSenders.remove(Sender.PLAYER); }
    protected void allowConsole() { this.allowedSenders.add(Sender.CONSOLE); }
    protected void disallowConsole() { this.allowedSenders.remove(Sender.CONSOLE); }
    protected void allowRemoteConsole() { this.allowedSenders.add(Sender.REMOTE_CONSOLE); }
    protected void disallowRemoteConsole() { this.allowedSenders.remove(Sender.REMOTE_CONSOLE); }

    private boolean canPlayerExecute() { return this.allowedSenders.contains(Sender.PLAYER); }
    private boolean canConsoleExecute() { return this.allowedSenders.contains(Sender.CONSOLE); }
    private boolean canRemoteConsoleExecute() { return this.allowedSenders.contains(Sender.REMOTE_CONSOLE); }

    protected void registerSubCommands(BaseSubCommand... baseSubCommands) {
        for (BaseSubCommand baseSubCommand : baseSubCommands) {
            registerSubCommand(baseSubCommand);
        }
    }

    protected void registerSubCommand(BaseSubCommand baseSubCommand) {
        this.subCommandsMap.put(baseSubCommand.getName().toLowerCase(), baseSubCommand);
    }

    /**
     * Sets the tab completer for this command.
     *
     * @param tabCompleter The tab completer to set.
     */
    public void setTabCompleter(TabCompleter tabCompleter) {
        this.tabCompleter = tabCompleter;
    }

    private boolean isAllowedSender(CommandSender sender) {
        if (sender instanceof Player) {
            return canPlayerExecute();
        } else if (sender instanceof ConsoleCommandSender) {
            return canConsoleExecute();
        } else if (sender instanceof RemoteConsoleCommandSender) {
            return canRemoteConsoleExecute();
        }
        return false;
    }

    private String getNotAllowedMessage(CommandSender sender) {
        if (sender instanceof Player) {
            return notForPlayersMessage;
        } else if (sender instanceof ConsoleCommandSender) {
            return notForConsoleMessage;
        } else if (sender instanceof RemoteConsoleCommandSender) {
            return notForRCONMessage;
        }
        return null;
    }

    private boolean hasRequiredPermission(CommandSender sender) {
        return sender.hasPermission(this.permission);
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        return tabCompleter != null ? tabCompleter.onTabComplete(sender, this, alias, args) : super.tabComplete(sender, alias, args);
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String label, String[] args) {
        if (!isAllowedSender(sender)) {
            String notAllowedMessage = getNotAllowedMessage(sender);
            if (notAllowedMessage != null) {
                sender.sendMessage(notAllowedMessage);
            }
            return false;
        }

        if (this.disabled) {
            sender.sendMessage(this.disabledMessage);
            return false;
        }

        if (this.requiredOp && !sender.isOp() || !hasRequiredPermission(sender)) {
            sender.sendMessage(String.format(this.insufficientPermissionsMessage, permission));
            return false;
        }

        String subCommandName = args.length > 0 ? args[0].toLowerCase() : "";
        BaseSubCommand subCommand = subCommandsMap.get(subCommandName);

        if (subCommand != null) {
            String[] subCommandArgs = Arrays.copyOfRange(args, 1, args.length);
            if (!subCommand.isAllowedSender(sender)) {
                String notAllowedMessage = getNotAllowedMessage(sender);
                if (notAllowedMessage != null) {
                    sender.sendMessage(notAllowedMessage);
                }
                return false;
            }
            if (subCommand.isDisabled()) {
                sender.sendMessage(this.disabledMessage);
                return false;
            }
            if (subCommand.isRequiredOp() && !sender.isOp() || !subCommand.hasRequiredPermission(sender)) {
                sender.sendMessage(String.format(this.insufficientPermissionsMessage, subCommand.getPermission()));
                return false;
            }
            subCommand.execute(new ExecutionParameters(subCommandArgs, sender));
        } else if (subCommandName.isEmpty()) {
            this.execute(new ExecutionParameters(args, sender));
        } else {
            printHelp(sender);
        }

        return true;
    }

    private void initializeCommandFromAnnotation() {
        CommandInfo annotation = this.getClass().getAnnotation(CommandInfo.class);
        if (annotation != null) {
            String name = annotation.name();
            String description = annotation.description();
            String permission = annotation.permission();
            String usage = annotation.usage();
            List<String> aliases = Arrays.asList(annotation.aliases());

            this.setName(name);
            this.setLabel(name);
            this.setDescription(description);
            this.setUsage(usage);
            this.setAliases(aliases);

            this.permission = permission;
            this.allowedSenders.clear();
            this.allowedSenders.addAll(List.of(annotation.allowed()));
        }
    }

}