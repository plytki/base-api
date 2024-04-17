package dev.plytki.baseapi.inventories.util;

import org.bukkit.inventory.ItemStack;

public class ItemUtil {

    public static ItemStack deserialize(String base64) {
        try {
            return Base64.decode(base64, ItemStack.class);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static String serialize(ItemStack itemStack) {
        try {
            return Base64.encode(itemStack);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
