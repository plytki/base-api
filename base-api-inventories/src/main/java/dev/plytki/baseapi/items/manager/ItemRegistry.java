package dev.plytki.baseapi.items.manager;

import dev.plytki.baseapi.items.BaseItem;
import dev.plytki.baseapi.items.exception.ItemRegisteredException;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class ItemRegistry {

    private final List<BaseItem> items = new ArrayList<>();

    private final Plugin plugin;

    public ItemRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public BaseItem getItem(String id) {
        return this.items.stream().filter(baseItem -> baseItem.getId().equals(id)).findFirst().orElse(null);
    }

    private void registerItem(BaseItem item) throws ItemRegisteredException {
        if (this.items.stream().anyMatch(registeredItem -> registeredItem.getId().equals(item.getId()))) {
            throw new ItemRegisteredException();
        } else {
            this.items.add(item);
        }
    }

    public void registerItems(BaseItem... items) throws ItemRegisteredException {
        for (BaseItem item : items) {
            registerItem(item);
        }
        this.plugin.getLogger().log(Level.INFO, "Items successfully registered!");
    }

    public List<BaseItem> getItems() {
        return items;
    }

    public Plugin getPlugin() {
        return plugin;
    }

}