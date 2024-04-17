package dev.plytki.baseapi.inventories.util.legacy;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

public class LegacySkullBuilder {

    private static final String TEXTURE_URL_BASE = "https://textures.minecraft.net/texture/";

    private final String url;

    public LegacySkullBuilder(String url) {
        this.url = url.startsWith(TEXTURE_URL_BASE) ? url : TEXTURE_URL_BASE + url;
    }

    public LegacySkullBuilder(ItemStack itemStack) {
        this.url = extractSkullTexture(itemStack);
    }

    public ItemStack getSkull() {
        ItemStack skull = new ItemStack(Material.valueOf("SKULL_ITEM"), 1, (short)3);
        if (url == null || url.isEmpty())
            return skull;
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = java.util.Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", url).getBytes());
        profile.getProperties().put("textures", new Property("textures", new String(encodedData)));
        Field profileField = null;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            profileField.set(skullMeta, profile);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        skull.setItemMeta(skullMeta);
        return skull;
    }

    public String extractSkullTexture(ItemStack skull) {
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        GameProfile gp = null;
        Field profileField = null;
        try {
            profileField = skullMeta.getClass().getDeclaredField("profile");
        } catch (NoSuchFieldException | SecurityException e) {
            e.printStackTrace();
        }
        profileField.setAccessible(true);
        try {
            gp = (GameProfile) profileField.get(skullMeta);
        } catch (IllegalArgumentException | IllegalAccessException e) {
            e.printStackTrace();
        }
        String texture = null;
        for (Property textures : gp.getProperties().get("textures")) {
            if (textures.getName().equals("textures")) {
                String textureString = new String(Base64.getDecoder().decode(textures.getValue()));
                texture = textureString.substring(22, textureString.length()-4);
            }
        }
        return texture;
    }

    public String getUrl() {
        return url;
    }

}
