package dev.plytki.baseapi.inventories.manager;

import dev.plytki.baseapi.inventories.model.BaseInventory;
import lombok.Data;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.*;

@Data
public class InventoryRegistry implements Listener {

    private final Plugin plugin;
    private final Set<BaseInventory> liveInventories = new HashSet<>();
    private final Map<UUID, Deque<BaseInventory>> inventoryTracker = new HashMap<>();

    public InventoryRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    public void trackInventory(BaseInventory baseInventory, UUID viewer) {
        Deque<BaseInventory> viewerInventories = inventoryTracker.computeIfAbsent(viewer, key -> new ArrayDeque<>());
        if (!viewerInventories.isEmpty() && viewerInventories.element().getClass().equals(baseInventory.getClass()))
            return;
        viewerInventories.push(baseInventory);
    }

    public Deque<BaseInventory> getPreviousInventories(UUID viewer) {
        return inventoryTracker.get(viewer);
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