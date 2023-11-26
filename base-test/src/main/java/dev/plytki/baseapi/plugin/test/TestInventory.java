package dev.plytki.baseapi.plugin.test;

import dev.plytki.baseapi.inventories.manager.InventoryRegistry;
import dev.plytki.baseapi.inventories.model.BaseInventory;
import dev.plytki.baseapi.inventories.util.ItemBuilder;
import org.bukkit.Material;

public class TestInventory extends BaseInventory {

    public TestInventory(InventoryRegistry inventoryRegistry) {
        super(inventoryRegistry, "Test Inventory", 3);
        cancel(Slots.ALL);

        registerTask(() -> {
            System.out.println("test");
            System.out.println("test 2");
        }, 10);

        registerSlot(1, new ItemBuilder(Material.GREEN_WOOL).toItemStack(), event -> {
            event.getWhoClicked().sendMessage("§dYou clicked slot 1!");
        });
    }

}