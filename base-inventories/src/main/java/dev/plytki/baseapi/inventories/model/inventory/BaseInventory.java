package dev.plytki.baseapi.inventories.model.inventory;

import dev.plytki.baseapi.inventories.manager.InventoryRegistry;
import dev.plytki.baseapi.inventories.model.task.BaseTaskManager;
import dev.plytki.baseapi.inventories.util.ItemBuilder;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Represents an abstract base inventory class that provides functionality to
 * create and manage custom inventories within a Minecraft plugin. This class
 * is designed to handle inventory clicks, closes, and drags along with special
 * inventory properties such as cancellation policies and click delays.
 */
@Getter
public abstract class BaseInventory implements IBaseInventory {

    private Inventory inventory;
    private final InventoryRegistry registry;
    private final InventoryHandler inventoryHandler;
    private final BaseTaskManager taskManager;

    private int refreshRate = 0;
    private long refreshTaskId;

    /**
     * Constructs an instance of BaseInventory with specified parameters.
     *
     * @param inventoryRegistry The registry handling all inventories.
     */
    public BaseInventory(InventoryRegistry inventoryRegistry) {
        this.registry = inventoryRegistry;
        Plugin plugin = inventoryRegistry.getPlugin();
        this.inventoryHandler = new InventoryHandler(this, plugin);
        this.taskManager = new BaseTaskManager(plugin);
        this.registry.addLiveInventory(this);
    }

    protected abstract InventoryProperties properties(InventoryProperties properties);

    protected abstract void update();

    protected void create() {
        InventoryProperties properties = properties(new InventoryProperties("default", 3));
        this.inventoryHandler.cancelPolicy(properties.cancelPolicy());
        this.inventoryHandler.cancelPolicyPlayer(properties.cancelPolicyPlayer());
        this.inventory = Bukkit.createInventory(null, properties.getLines() * 9, properties.getName());
        update();
    }

    @Override
    public void updateInventory() {
        this.inventoryHandler.updateInventory();
    }

    @Override
    public void close() {
        this.inventoryHandler.close();
    }

    @Override
    public void destroy() {
        this.inventoryHandler.destroy();
    }

    @Override
    public void debug(Consumer<InventoryClickEvent> eventConsumer) {
        inventoryHandler.debug(eventConsumer);
    }

    @Override
    public void setAll(ItemStack itemStack) {
        boolean isAir = itemStack == null || itemStack.getType() == Material.AIR;
        IntStream.range(0, this.inventory.getSize())
                .forEach(i -> {
                    if (isAir) {
                        this.inventory.setItem(i, new ItemStack(Material.AIR));
                    } else {
                        this.inventory.setItem(i, itemStack.clone());
                    }
                });
    }

    @Override
    public void setAll(Material material, String displayName) {
        setAll(new ItemBuilder(material).setName(displayName).toItemStack());
    }

    @Override
    public void setAll(Material material) {
        setAll(new ItemBuilder(material).setName(" ").toItemStack());
    }

    @Override
    public void setItem(int slot, ItemStack itemStack) {
        getInventory().setItem(slot, itemStack);
    }

    @Override
    public void setClickCooldown(long millis) {
        this.inventoryHandler.setClickCooldown(millis);
    }

    @Override
    public long runTask(Runnable runnable, long delay, long period) {
        return taskManager.runTask(runnable, delay, period);
    }

    @Override
    public long runTask(long taskId, Runnable runnable, long delay) {
        return taskManager.runTask(taskId, runnable, delay);
    }

    @Override
    public long runTask(long taskId, Runnable runnable, long delay, long period) {
        return taskManager.runTask(taskId, runnable, delay, period);
    }

    @Override
    public long runTask(Runnable runnable, long delay) {
        return taskManager.runTask(runnable, delay);
    }

    @Override
    public boolean stopTask(long taskID) {
        return taskManager.stopTask(taskID);
    }

    @Override
    public BukkitTask getTask(long taskID) {
        return taskManager.getTask(taskID);
    }

    @Override
    public void setItem(int[] slots, ItemStack itemStack, Integer... skippedSlots) {
        Set<Integer> skippedSlotsSet = new HashSet<>(Arrays.asList(skippedSlots));
        Arrays.stream(slots)
                .filter(i -> !skippedSlotsSet.contains(i))
                .forEach(i -> {
                    this.inventory.setItem(i, itemStack.clone());
                });
    }

    @Override
    public void setItem(int from, int to, ItemStack itemStack, Integer... skippedSlots) {
        Set<Integer> skippedSlotsSet = new HashSet<>(Arrays.asList(skippedSlots));
        IntStream.rangeClosed(from, to)
                .filter(i -> !skippedSlotsSet.contains(i))
                .forEach(i -> {
                    this.inventory.setItem(i, itemStack.clone());
                });
    }

    /**
     * This method gathers all inventory slots starting from "from" param skipped by all "skippedSlots" slots.
     * @param from - From which slot it should start.
     * @param itemStack - The item which will be set as a blank at the slots positions.
     * @param skippedSlots - These are skipped slots that are excluded from the array.
     */
    @Override
    public void setItem(int from, ItemStack itemStack, Integer... skippedSlots) {
        setItem(from, getInventory().getSize()-1, itemStack, skippedSlots);
    }

    /**
     * Sets the cancellation policy for inventory slots.
     *
     * @param policy The cancellation policy to set.
     */
    @Override
    public void cancelPolicy(CancelPolicy policy) {
        this.inventoryHandler.cancelPolicy(policy);
    }

    /**
     * Cancel player inventory slots of type Slots.
     */
    @Override
    public void cancelPolicyPlayer(CancelPolicy policy) {
        this.inventoryHandler.cancelPolicyPlayer(policy);
    }

    @Override
    public void setItemsDrag(boolean enabled) {
        this.inventoryHandler.setItemsDrag(enabled);
    }

    @Override
    public void setCooldownMessage(boolean enabled) {
        this.inventoryHandler.setCooldownMessage(enabled);
    }

    /**
     * Checks if the player is currently in the inventory.
     * @param player - Player which is being checked.
     * @return - If the player is in the current BossInventory.
     */
    @Override
    public boolean isInInventory(Player player) {
        return this.inventoryHandler.isInInventory(player);
    }

    @Override
    public boolean isInAnotherInventory(Player player) {
        return this.inventoryHandler.isInAnotherInventory(player);
    }

    /**
     * Registers a click event handler for a range of inventory slots.
     *
     * @param from     The starting slot index to register the event for.
     * @param to       The ending slot index to register the event for.
     * @param function The function to execute on a click event.
     */
    @Override
    public void registerSlots(int from, int to, Consumer<InventoryClickEvent> function) {
        for (int i = from; i <= to; i++) {
            registerSlot(i, function);
        }
    }

    /**
     * Registers a click event handler for specific inventory slots.
     *
     * @param slots    An array of slots to register the event for.
     * @param function The function to execute on a click event.
     */
    @Override
    public void registerSlots(int[] slots, Consumer<InventoryClickEvent> function) {
        for (int slot : slots) {
            registerSlot(slot, function);
        }
    }

    /**
     * Registers a click event handler for a specific inventory slot.
     *
     * @param slot     The slot index to register the event for.
     * @param function The function to execute on a click event.
     */
    @Override
    public void registerSlot(int slot, Consumer<InventoryClickEvent> function) {
        this.inventoryHandler.registerSlot(slot, function);
    }

    @Override
    public void registerPlayerSlot(int slot, Consumer<InventoryClickEvent> function) {
        this.inventoryHandler.registerPlayerSlot(slot, function);
    }

    /**
     * Register the slot with custom event handler.
     * @param slot - Slot to be registered.
     * @param itemStack - ItemStack will be set in the inventory at the slot position.
     * @param function - Function to be executed.
     */
    @Override
    public void registerSlot(int slot, ItemStack itemStack, Consumer<InventoryClickEvent> function) {
        this.inventory.setItem(slot, itemStack);
        registerSlot(slot, function);
    }

    @Override
    public void registerSlots(int[] slots, ItemStack itemStack, Consumer<InventoryClickEvent> function) {
        for (int slot : slots) {
            registerSlot(slot, itemStack, function);
        }
    }

    @Override
    public void registerSlots(int from, int to, ItemStack itemStack, Consumer<InventoryClickEvent> function) {
        for (int i = from; i <= to; i++) {
            registerSlot(i, itemStack, function);
        }
    }

    @Override
    public void registerPlayerSlots(int[] slots, Consumer<InventoryClickEvent> function) {
        for (int slot : slots) {
            registerPlayerSlot(slot, function);
        }
    }

    @Override
    public void registerPlayerSlots(int from, int to, Consumer<InventoryClickEvent> function) {
        for (int i = from; i <= to; i++) {
            registerPlayerSlot(i, function);
        }
    }

    /**
     * Registers a custom handler to be called upon inventory close events.
     *
     * @param function The function that will be executed on inventory close.
     */
    @Override
    public void onInventoryClose(Consumer<InventoryCloseEvent> function) {
        this.inventoryHandler.onInventoryClose(function);
    }

    /**
     * Unregisters a slot's custom event handler from the list of registered slots.
     *
     * @param slot The slot number to unregister.
     */
    @Override
    public void unregisterSlot(int slot) {
        this.inventoryHandler.unregisterSlot(slot);
    }

    /**
     * Unregister a slot custom handler from current list of player inventory registered slots.
     * @param slot - Slot to be unregistered.
     */
    @Override
    public void unregisterPlayerSlot(int slot) {
        this.inventoryHandler.unregisterPlayerSlot(slot);
    }

    /**
     * Unregisters a slot's custom event handler and sets the specified ItemStack to the slot.
     *
     * @param slot      The slot number to unregister.
     * @param itemStack The ItemStack to set at the slot.
     */
    @Override
    public void unregisterSlot(int slot, ItemStack itemStack) {
        unregisterSlot(slot);
        setItem(slot, itemStack);
    }

    /**
     * Opens the current BaseInventory instance to the specified player.
     *
     * @param player Varargs array of players to open the inventory for.
     */
    @Override
    public void open(HumanEntity... player) {
        for (HumanEntity humanEntity : player) {
            humanEntity.openInventory(this.inventory);
            this.registry.trackInventory(this, humanEntity.getUniqueId());
            this.inventoryHandler.getViewers().add(humanEntity.getUniqueId());
        }
    }

    @Override
    public void openPreviousInventoryIfPresent(HumanEntity viewer) {
        Deque<BaseInventory> viewerInventories = registry.getInventoryTracker().get(viewer.getUniqueId());
        if (viewerInventories != null && !viewerInventories.isEmpty()) {
            viewerInventories.pop();
            BaseInventory poll = viewerInventories.poll();
            if (poll != null)
                poll.open(viewer);
        }
    }

    public void refreshRate(int ticks) {
        if (this.refreshTaskId != 0) {
            stopTask(this.refreshTaskId);
        }
        this.refreshRate = ticks;
        if (this.refreshRate > 0) {
            this.refreshTaskId = runTask(new Random().nextLong(), this::update, ticks, ticks);
        }
    }

    @Override
    public final int getPlayerInventorySlots() {
        return 36;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    @Override
    public List<Player> getPlayers() {
        return this.inventoryHandler.getViewers().stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .map(OfflinePlayer::getPlayer)
                .toList();
    }

    /**
     * @param from - From which slot it should start.
     * @param to - At which slot it should end.
     * @param skippedSlots - These are skipped slots that are excluded from the array.
     * @return - Returns filtered slots by skippedSlots.
     */
    @Override
    public int[] getSlots(int from, int to, Integer... skippedSlots) {
        Set<Integer> skippedSlotsSet = new HashSet<>(Arrays.asList(skippedSlots));
        return IntStream.rangeClosed(from, to)
                .filter(i -> !skippedSlotsSet.contains(i))
                .toArray();
    }

}