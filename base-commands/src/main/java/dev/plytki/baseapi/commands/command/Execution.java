package dev.plytki.baseapi.commands.command;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.function.Predicate;

public class Execution {

    private final String[] args;
    private final CommandSender sender;

    public Execution(String[] args, CommandSender sender) {
        this.args = Objects.requireNonNull(args, "args must not be null");
        this.sender = Objects.requireNonNull(sender, "sender must not be null");
    }

    private boolean isValidIndex(int index) {
        return index >= 0 && index < args.length;
    }

    public String getString(int index) {
        if (index < args.length) {
            return args[index];
        }
        return "";
    }

    public Optional<String> getOptionalString(int index) {
        if (index < args.length && args[index] != null && !args[index].isEmpty()) {
            return Optional.of(args[index]);
        }
        return Optional.empty();
    }

    public int getInt(int index) {
        try {
            return Integer.parseInt(getString(index));
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    public Optional<Integer> getOptionalInt(int index) {
        if (index < args.length) {
            return Optional.of(getInt(index));
        }
        return Optional.empty();
    }

    public double getDouble(int index) {
        try {
            return Double.parseDouble(getString(index));
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public Optional<Double> getOptionalDouble(int index) {
        if (index < args.length) {
            return Optional.of(getDouble(index));
        }
        return Optional.empty();
    }

    public boolean getBoolean(int index, Predicate<String> verify) {
        String value = getString(index).toLowerCase();
        return verify.test(value);
    }

    public Optional<Boolean> getOptionalBoolean(int index, Predicate<String> verify) {
        if (index < args.length) {
            return Optional.of(getBoolean(index, verify));
        }
        return Optional.empty();
    }


    public Player getPlayer(int index) {
        String name = getString(index);
        return Bukkit.getPlayer(name);
    }

    public Optional<Player> getOptionalPlayer(int index) {
        return Optional.ofNullable(Bukkit.getPlayer(getString(index)));
    }

    public List<String> getStringsFrom(int index) {
        return new ArrayList<>(Arrays.asList(args).subList(index, args.length));
    }

    public Optional<UUID> getOptionalUUID(int index) {
        try {
            return Optional.of(UUID.fromString(getString(index)));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    public String getJoinedStrings(int startIndex, String delimiter) {
        if (startIndex < args.length) {
            return String.join(delimiter, Arrays.copyOfRange(args, startIndex, args.length));
        }
        return "";
    }

    public <T extends Enum<T>> T getEnum(Class<T> enumClass, int index) {
        if (isValidIndex(index)) {
            String value = getString(index).toUpperCase();
            return EnumSet.allOf(enumClass).stream()
                    .filter(e -> e.name().equals(value))
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public Optional<Material> getOptionalMaterial(int index) {
        return Optional.ofNullable(Material.matchMaterial(getString(index)));
    }

    public boolean isEmpty() {
        return args.length == 0;
    }

    public CommandSender getSender() {
        return sender;
    }

    public Player getSenderPlayer() {
        return (Player) sender;
    }

    public ConsoleCommandSender getSenderConsole() {
        return (ConsoleCommandSender) sender;
    }

    public int length() {
        return args.length;
    }

    public List<String> getRawArgsList() {
        return new ArrayList<>(Arrays.asList(args));
    }

}