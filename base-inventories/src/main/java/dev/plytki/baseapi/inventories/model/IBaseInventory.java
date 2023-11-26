package dev.plytki.baseapi.inventories.model;

import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.function.Consumer;

public interface IBaseInventory {

    void registerSlot(int slotIndex, Consumer<InventoryClickEvent> function);
    void registerSlot(int slotIndex, ItemStack itemStack, Consumer<InventoryClickEvent> function);
    void registerSlots(int[] slots, Consumer<InventoryClickEvent> function);
    void registerSlots(int[] slots, ItemStack itemStack, Consumer<InventoryClickEvent> function);
    void registerSlots(int from, int to, Consumer<InventoryClickEvent> function);
    void registerSlots(int from, int to, ItemStack itemStack, Consumer<InventoryClickEvent> function);

    void registerPlayerSlot(int slotIndex, Consumer<InventoryClickEvent> function);
    void registerPlayerSlots(int[] slots, Consumer<InventoryClickEvent> function);
    void registerPlayerSlots(int from, int to, Consumer<InventoryClickEvent> function);

}