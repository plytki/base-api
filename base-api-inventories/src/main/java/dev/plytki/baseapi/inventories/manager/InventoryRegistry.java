package dev.plytki.baseapi.inventories.manager;

import dev.plytki.baseapi.inventories.model.BaseInventory;
import lombok.Data;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.Set;

@Data
public class InventoryRegistry {

    private final Plugin plugin;
    private final Set<BaseInventory> liveInventories = new HashSet<>();

    public InventoryRegistry(Plugin plugin) {
        this.plugin = plugin;
    }

    public void addLiveInventory(BaseInventory baseInventory) {
        this.liveInventories.add(baseInventory);
    }

    public void removeLiveInventory(BaseInventory baseInventory) {
        this.liveInventories.remove(baseInventory);
    }

    public void destroyAllLiveInventories() {
        for (BaseInventory liveInventory : new HashSet<>(this.liveInventories)) {
            liveInventory.destroy();
        }
    }

}