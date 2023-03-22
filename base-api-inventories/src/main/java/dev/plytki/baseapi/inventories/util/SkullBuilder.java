package dev.plytki.baseapi.inventories.util;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.lang.reflect.Field;
import java.util.Base64;
import java.util.UUID;

public class SkullBuilder {

    private String url;

    public SkullBuilder(String url) {
        this.url = url.replaceAll("http://textures.minecraft.net/texture/", "");
    }

    public SkullBuilder(ItemStack itemStack) {
        this.url = getSkullTexture(itemStack);
    }

    public ItemStack getSkull() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (url == null || url.isEmpty())
            return skull;
        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        GameProfile profile = new GameProfile(UUID.randomUUID(), null);
        byte[] encodedData = Base64.getEncoder().encode(String.format("{textures:{SKIN:{url:\"%s\"}}}", "http://textures.minecraft.net/texture/" + url).getBytes());
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

    public String getSkullTexture(ItemStack skull) {
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
        for (Property textures : gp.getProperties().get("textures")) {
            if (textures.getName().equals("textures")) {
                String textureString = new String(Base64.getDecoder().decode(textures.getValue()));
                this.url = textureString.substring(22, textureString.length()-4);
            }
        }
        return this.url;
    }

    public String getUrl() {
        return url;
    }

}