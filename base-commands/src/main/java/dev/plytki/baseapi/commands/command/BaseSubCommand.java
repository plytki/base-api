package dev.plytki.baseapi.commands.command;

import lombok.Getter;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Getter
public abstract class BaseSubCommand implements Executable {

    private final BaseCommand superCommand;
    private final String name;
    private final String permission;
    private final Set<Sender> allowedSenders = EnumSet.noneOf(Sender.class);
    private boolean disabled;
    private boolean requiredOp;

    protected BaseSubCommand(BaseCommand superCommand) {
        this.superCommand = superCommand;

        SubCommandInfo annotation = this.getClass().getAnnotation(SubCommandInfo.class);
        String name = annotation.name();
        String permission = annotation.permission();

        this.name = name;
        this.permission = permission;
        this.allowedSenders.clear();
        this.allowedSenders.addAll(Arrays.asList(annotation.allowed()));
    }

    public void disabled(boolean disabled) {
        this.disabled = disabled;
    }

    protected void requiredOp(boolean required) {
        this.requiredOp = required;
    }

    protected boolean hasRequiredPermission(CommandSender sender) {
        return sender.hasPermission(this.permission);
    }

    protected void allow(Sender... senders) {
        this.allowedSenders.addAll(Arrays.asList(senders));
    }

    protected void disallow(Sender... senders) {
        Arrays.asList(senders).forEach(this.allowedSenders::remove);
    }

    protected boolean isAllowedSender(CommandSender sender) {
        if (sender instanceof Player) {
            return canPlayerExecute();
        } else if (sender instanceof ConsoleCommandSender) {
            return canConsoleExecute();
        } else if (sender instanceof RemoteConsoleCommandSender) {
            return canRemoteConsoleExecute();
        }
        return false;
    }

    private boolean canPlayerExecute() { return this.allowedSenders.contains(Sender.PLAYER); }
    private boolean canConsoleExecute() { return this.allowedSenders.contains(Sender.CONSOLE); }
    private boolean canRemoteConsoleExecute() { return this.allowedSenders.contains(Sender.REMOTE_CONSOLE); }

}