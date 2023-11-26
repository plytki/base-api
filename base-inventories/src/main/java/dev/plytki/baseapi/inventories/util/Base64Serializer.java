package dev.plytki.baseapi.inventories.util;

import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.*;
import java.util.logging.Level;

public class Base64Serializer {

    public static String toBase64(Inventory inventory) {
        return encodeObject(stream -> {
            stream.writeInt(inventory.getSize());
            for (int i = 0; i < inventory.getSize(); i++) {
                stream.writeObject(inventory.getItem(i));
            }
        });
    }

    public static Inventory fromBase64(String data) {
        return decodeObject(data, stream -> {
            Inventory inventory = Bukkit.getServer().createInventory(null, stream.readInt());
            for (int i = 0; i < inventory.getSize(); i++) {
                inventory.setItem(i, (ItemStack) stream.readObject());
            }
            return inventory;
        });
    }

    public static String toBase64(ItemStack itemStack) {
        return encodeObject(stream -> stream.writeObject(itemStack));
    }

    public static ItemStack fromBase64ItemStack(String data) {
        return (ItemStack) decodeObject(data, BukkitObjectInputStream::readObject);
    }

    private static String encodeObject(ThrowingConsumer<BukkitObjectOutputStream> write) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            write.accept(dataOutput);
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to serialize object.", e);
            throw new IllegalArgumentException("Unable to serialize object.", e);
        }
    }

    private static <T> T decodeObject(String data, ThrowingFunction<BukkitObjectInputStream, T> read) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return read.apply(dataInput);
        } catch (IOException | ClassNotFoundException e) {
            Bukkit.getLogger().log(Level.SEVERE, "Unable to deserialize object.", e);
            throw new IllegalArgumentException("Unable to deserialize object.", e);
        }
    }

    @FunctionalInterface
    public interface ThrowingConsumer<T> {
        void accept(T t) throws IOException;
    }

    @FunctionalInterface
    public interface ThrowingFunction<T, R> {
        R apply(T t) throws IOException, ClassNotFoundException;
    }

}