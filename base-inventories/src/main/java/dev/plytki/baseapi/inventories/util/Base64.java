package dev.plytki.baseapi.inventories.util;

import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * This class provides utility methods for encoding and decoding data using Base64 encoding.
 */
public class Base64 {


    /**
     * Encodes the given object using Base64 encoding.
     *
     * @param contents the object to encode
     * @param <T>      the type of the object to encode
     * @return the Base64-encoded representation of the object
     * @throws Exception if there is an error while encoding the object
     */
    public static <T> String encode(T contents) throws Exception {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);
            dataOutput.writeObject(contents);
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            throw new Exception("Unable to save item stacks.", e);
        }
    }

    /**
     * Decodes a Base64 encoded string into the specified format.
     *
     * @param data   The Base64 encoded string to decode.
     * @param format The class object representing the format to decode the data into.
     * @param <T>    The type to cast the decoded object into.
     * @return The decoded object in the specified format.
     * @throws Exception If an error occurs while decoding the string.
     */
    public static <T> T decode(String data, Class<T> format) throws Exception {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            Object o = dataInput.readObject();
            T cast = format.cast(o);
            dataInput.close();
            return cast;
        } catch (ClassNotFoundException | IOException e) {
            throw new Exception("Unable to convert from Base64.", e);
        }
    }


}
