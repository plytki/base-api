package dev.plytki.baseapi.inventories.model.inventory;

import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.function.Consumer;

public interface IBaseInventory extends InventoryHolder {

    Inventory getInventory();
    void debug(Consumer<InventoryClickEvent> eventConsumer);
    void onInventoryClose(Consumer<InventoryCloseEvent> function);

    int[] getSlots(int from, int to, Integer... skippedSlots);

    void setAll(ItemStack itemStack);
    void setAll(Material material, String displayName);
    void setAll(Material material);

    void setItem(int slot, ItemStack itemStack);
    void setItem(int[] slots, ItemStack itemStack, Integer... skippedSlots);
    void setItem(int from, ItemStack itemStack, Integer... skippedSlots);
    void setItem(int from, int to, ItemStack itemStack, Integer... skippedSlots);

    void setClickCooldown(long ms);

    long runTask(long taskId, Runnable runnable, long delay);
    long runTask(long taskId, Runnable runnable, long delay, long period);
    long runTask(Runnable runnable, long delay);
    long runTask(Runnable runnable, long delay, long period);

    boolean stopTask(long taskID);
    BukkitTask getTask(long taskID);


    void cancelPolicy(CancelPolicy policy);
    void cancelPolicyPlayer(CancelPolicy policy);

    void setItemsDrag(boolean enabled);
    void setCooldownMessage(boolean enabled);

    boolean isInInventory(Player player);
    boolean isInAnotherInventory(Player player);

    void registerSlots(int from, int to, Consumer<InventoryClickEvent> function);
    void registerSlots(int[] slots, Consumer<InventoryClickEvent> function);
    void registerSlot(int slot, Consumer<InventoryClickEvent> function);
    void registerSlot(int slot, ItemStack itemStack, Consumer<InventoryClickEvent> function);
    void registerSlots(int[] slots, ItemStack itemStack, Consumer<InventoryClickEvent> function);
    void registerSlots(int from, int to, ItemStack itemStack, Consumer<InventoryClickEvent> function);
    void unregisterSlot(int slot);
    void unregisterSlot(int slot, ItemStack itemStack);

    void registerPlayerSlot(int slot, Consumer<InventoryClickEvent> function);
    void registerPlayerSlots(int[] slots, Consumer<InventoryClickEvent> function);
    void registerPlayerSlots(int from, int to, Consumer<InventoryClickEvent> function);
    void unregisterPlayerSlot(int slot);


    void updateInventory();
    int getPlayerInventorySlots();

    List<Player> getPlayers();

    void open(HumanEntity... player);
    void openPreviousInventoryIfPresent(HumanEntity viewer);
    void close();
    void destroy();

}
