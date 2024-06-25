package dev.plytki.baseapi.commands.command;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class BaseCommand extends Command implements Executable, TabCompleter {

    private static final String DEFAULT_NOT_AVAILABLE = "§cCommand is not available for %s!";
    private static final String DEFAULT_DISABLED_MESSAGE = "§cCommand is currently disabled!";
    private static final String DEFAULT_PERMISSION_MESSAGE = "§cInsufficient permissions! (%s)";


    @Getter
    private final Set<Sender> allowedSenders = EnumSet.noneOf(Sender.class);
    private boolean disabled;
    private boolean requiredOp;
    private CommandType commandType;

    @Setter
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
        this.commandType = CommandType.DEFAULT;
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

    public void commandType(CommandType commandType) {
        this.commandType = commandType;
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

    protected void allowOnly(Sender... senders) {
        this.allowedSenders.clear();
        this.allowedSenders.addAll(Arrays.asList(senders));
    }

    protected void disallow(Sender... senders) {
        Arrays.asList(senders).forEach(this.allowedSenders::remove);
    }

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
        for (String alias : baseSubCommand.getAliases()) {
            this.subCommandsMap.put(alias.toLowerCase(), baseSubCommand);
        }
    }

    @Override
    public void printHelp(CommandSender sender) {
        sender.sendMessage("§7You are using SubCommand type command.");
        sender.sendMessage("§7This is a default message.");
        sender.sendMessage("§7You can modify it by overriding `BaseCommand#printHelp` method");
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

    private boolean hasPermission(CommandSender sender) {
        if (this.permission.trim().isEmpty())
            return true;
        return sender.hasPermission(this.permission);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        return tabCompleter != null ? tabCompleter.onTabComplete(sender, this, alias, args) : super.tabComplete(sender, alias, args);
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
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

        boolean dontHaveRequiredOP = this.requiredOp && !sender.isOp();
        boolean dontHavePermission = !hasPermission(sender);
        if (dontHaveRequiredOP || dontHavePermission) {
            sender.sendMessage(String.format(this.insufficientPermissionsMessage, permission));
            return false;
        }
        switch (commandType) {
            case DEFAULT -> {
                this.execute(new Execution(args, sender));
            }
            case SUBCOMMAND -> {
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
                    subCommand.execute(new Execution(subCommandArgs, sender));
                } else if (subCommandName.isEmpty()) {
                    this.execute(new Execution(args, sender));
                } else {
                    printHelp(sender);
                }
            }
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
            this.allowedSenders.addAll(Arrays.asList(annotation.allowed()));
        }
    }

}