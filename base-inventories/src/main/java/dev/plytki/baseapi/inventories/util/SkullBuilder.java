package dev.plytki.baseapi.inventories.util;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.UUID;

public class SkullBuilder {

    private static final String TEXTURE_URL_BASE = "https://textures.minecraft.net/texture/";

    private final String url;

    public SkullBuilder(String url) {
        this.url = url.startsWith(TEXTURE_URL_BASE) ? url : TEXTURE_URL_BASE + url;
    }

    public SkullBuilder(ItemStack itemStack) {
        this.url = extractSkullTexture(itemStack);
    }

    public ItemStack getSkull() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        if (url == null || url.isEmpty())
            return skull;

        ItemMeta itemMeta = skull.getItemMeta();
        if (!(itemMeta instanceof SkullMeta skullMeta)) {
            return skull;
        }

        try {
            skullMeta.setPlayerProfile(Bukkit.createProfile(UUID.randomUUID(), null));
            PlayerTextures textures = skullMeta.getPlayerProfile().getTextures();
            textures.setSkin(new URL(url));
            skull.setItemMeta(skullMeta);
        } catch (MalformedURLException e) {
            e.printStackTrace();
            return skull;
        }

        return skull;
    }

    public String extractSkullTexture(ItemStack skull) {
        if (skull.getType() != Material.PLAYER_HEAD || !(skull.hasItemMeta())) {
            throw new IllegalArgumentException("The provided ItemStack is not a Player Skull.");
        }

        SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
        PlayerTextures textures = skullMeta.getPlayerProfile().getTextures();
        return textures.getSkin() != null ? textures.getSkin().toString() : "";
    }

    public String getUrl() {
        return url;
    }

}