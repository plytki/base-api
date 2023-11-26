package dev.plytki.baseapi.inventories.model;

import dev.plytki.baseapi.inventories.manager.InventoryRegistry;
import dev.plytki.baseapi.inventories.util.ItemBuilder;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

/**
 * Represents an abstract base inventory class that provides functionality to
 * create and manage custom inventories within a Minecraft plugin. This class
 * is designed to handle inventory clicks, closes, and drags along with special
 * inventory properties such as cancellation policies and click delays.
 */
public abstract class BaseInventory implements InventoryHolder {

    private final Plugin plugin;
    private final InventoryRegistry registry;
    private final Inventory inv;
    private final Set<UUID> viewers = new HashSet<>();
    private final InventoryListener listener;
    private final List<Runnable> refreshListeners = new ArrayList<>();
    private final Set<Consumer<InventoryClickEvent>> clickDebug = new HashSet<>();
    private final Map<Integer, Consumer<InventoryClickEvent>> clickEvents = new HashMap<>();
    private final Map<Integer, Consumer<InventoryClickEvent>> playerClickEvents = new HashMap<>();
    private final Set<Integer> registeredSlots = new HashSet<>();
    private final Set<Integer> registeredPlayerSlots = new HashSet<>();
    private final Set<Consumer<InventoryCloseEvent>> closeListeners = new HashSet<>();

    private CancellationPolicy slots;
    private CancellationPolicy playerSlots;
    private boolean cancelDrag;
    private final Map<Long, BukkitTask> tasks = new HashMap<>();

    private final Map<UUID, Long> delayMap = new HashMap<>();
    @Getter
    private long clickDelay = 0L;
    private boolean displayDelayMsg;
    private String delayMsg;
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private boolean persistent;

    /**
     * Constructs an instance of BaseInventory with specified parameters.
     *
     * @param inventoryRegistry The registry handling all inventories.
     * @param inventoryName The name of the inventory.
     * @param inventoryLines The size of the inventory in number of lines.
     * @param viewers The initial viewers of the inventory.
     */
    public BaseInventory(InventoryRegistry inventoryRegistry, Component inventoryName, int inventoryLines, Player... viewers) {
        this.inv = Bukkit.createInventory(null, inventoryLines * 9, inventoryName);
        this.registry = inventoryRegistry;
        this.listener = new InventoryListener();
        this.plugin = inventoryRegistry.getPlugin();
        this.slots = CancellationPolicy.NONE;
        this.playerSlots = CancellationPolicy.NONE;
        this.displayDelayMsg = true;
        setupMessages();
        registerListener();
        this.registry.addLiveInventory(this);
        this.persistent = false;
        open(viewers);
    }

    public BaseInventory(InventoryRegistry inventoryRegistry, String inventoryName, int inventoryLines, Player... viewers) {
        this(inventoryRegistry, Component.text(inventoryName), inventoryLines, viewers);
    }

    private void registerListener() {
        this.plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    private void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        this.clickDebug.forEach(consumer -> consumer.accept(e));

        boolean isPlayerInventory = e.getClickedInventory() instanceof PlayerInventory;
        CancellationPolicy slotsPolicy = isPlayerInventory ? this.playerSlots : this.slots;
        Set<Integer> registeredSlots = isPlayerInventory ? this.registeredPlayerSlots : this.registeredSlots;
        Map<Integer, Consumer<InventoryClickEvent>> clickSlotEvents = isPlayerInventory ? this.playerClickEvents : this.clickEvents;

        if (registeredSlots.contains(e.getSlot())) {
            if (slotsPolicy == CancellationPolicy.REGISTERED || slotsPolicy == CancellationPolicy.ALL) {
                e.setCancelled(true);
            }
        } else if (slotsPolicy == CancellationPolicy.UNREGISTERED || slotsPolicy == CancellationPolicy.ALL) {
            e.setCancelled(true);
        }

        clickSlotEvents.entrySet().stream()
                .filter(entry -> e.getSlot() == entry.getKey())
                .findFirst()
                .ifPresent(entry -> {
                    if (hasClickDelay(player)) {
                        e.setCancelled(true);
                        if (this.displayDelayMsg) {
                            player.sendMessage(String.format(this.delayMsg, getDelay(player) + this.clickDelay - System.currentTimeMillis()));
                        }
                    } else {
                        setClickDelay(player);
                        entry.getValue().accept(e);
                    }
                });
    }

    private void handleDrag(InventoryDragEvent e) {
        if (this.cancelDrag) {
            e.setCancelled(true);
        }
    }

    /**
     * Handles the closing of the base inventory for a player.
     * This method should be called when the inventory being closed is the base inventory.
     *
     * <p>
     * If the base inventory is non-persistent (this.persistent == false), the method removes
     * the player from the list of viewers and calls the registered close listeners. If there
     * are no viewers left, the base inventory is destroyed.
     * </p>
     *
     * <p>
     * If the base inventory is persistent (this.persistent == true), the method does not perform
     * any actions. The base inventory is not destroyed, and the close listeners are not called.
     * The handling of closing the base inventory when persistent is handled by the
     * handleCloseForAnyInventory method.
     * </p>
     *
     * @param e The InventoryCloseEvent representing the closing of the base inventory.
     */
    private void handleClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        this.viewers.remove(player.getUniqueId());

        if (!this.persistent) {
            if (this.viewers.isEmpty()) {
                destroy();
            }
            this.closeListeners.forEach(onCloseListener -> onCloseListener.accept(e));
        }
    }

    private void handleCloseForAnyInventory(InventoryCloseEvent e) {
        if (this.persistent && e.getReason() != InventoryCloseEvent.Reason.OPEN_NEW && this.viewers.isEmpty()) {
            destroy();
            Deque<BaseInventory> baseInventories = this.registry.getInventoryTracker().get(e.getPlayer().getUniqueId());
            if (!baseInventories.isEmpty())
                baseInventories.pop();
            this.closeListeners.forEach(onCloseListener -> onCloseListener.accept(e));
        }
    }

    private void setupMessages() {
        this.delayMsg = "§cYou have to wait %s ms!";
    }

    private void unregisterListener() {
        HandlerList.unregisterAll(this.listener);
    }

    public void debugClick(Consumer<InventoryClickEvent> eventConsumer) {
        this.clickDebug.add(eventConsumer);
    }

    public void setAllBlank(ItemStack itemStack) {
        boolean isAir = itemStack == null || itemStack.getType() == Material.AIR;
        IntStream.range(0, this.inv.getSize())
                .forEach(i -> {
                    if (isAir) {
                        this.inv.setItem(i, new ItemStack(Material.AIR));
                    } else {
                        this.inv.setItem(i, itemStack.clone());
                    }
                });
    }

    public void setAllBlank(Material material, String displayName) {
        setAllBlank(new ItemBuilder(material).setName(displayName).toItemStack());
    }

    public void setAllBlank(Material material) {
        setAllBlank(new ItemBuilder(material).setName(" ").toItemStack());
    }

    public void setItem(int slot, ItemStack itemStack) {
        getInventory().setItem(slot, itemStack);
    }

    public void setSlot(int slot, ItemStack itemStack) {
        setItem(slot, itemStack);
    }

    public void setClickDelay(long ms) {
        this.clickDelay = ms;
    }

    private void setClickDelay(Player player) {
        this.delayMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private void clicked(Player player) {
        this.delayMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    private boolean hasClickDelay(Player player) {
        return Optional.ofNullable(this.delayMap.get(player.getUniqueId()))
                .map(delay -> delay + this.clickDelay >= System.currentTimeMillis())
                .orElse(false);
    }

    private long getDelay(Player player) {
        return this.delayMap.getOrDefault(player.getUniqueId(), 0L);
    }

    public void registerTask(long taskID, Runnable runnable, long delay, long period) {
        cancelTask(taskID);
        this.tasks.put(taskID, scheduleTask(runnable, delay, period));
    }

    public void registerTask(Runnable runnable, long delay, long period) {
        registerTask(new Random().nextLong(), runnable, delay, period);
    }

    private BukkitTask scheduleTask(Runnable runnable, long delay, long period) {
        return period > 0
                ? new BukkitRunnable() {
            public void run() { runnable.run(); }
        }.runTaskTimer(this.plugin, delay, period)
                : new BukkitRunnable() {
            public void run() { runnable.run(); }
        }.runTaskLater(this.plugin, delay);
    }

    public void cancelTask(long taskID) {
        BukkitTask bukkitTask = this.tasks.remove(taskID);
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
    }

    public BukkitTask getTask(long taskID) {
        return this.tasks.get(taskID);
    }

    public void setItem(int[] slots, ItemStack itemStack, Integer... skippedSlots) {
        Set<Integer> skippedSlotsSet = new HashSet<>(Arrays.asList(skippedSlots));
        Arrays.stream(slots)
                .filter(i -> !skippedSlotsSet.contains(i))
                .forEach(i -> {
                    this.inv.setItem(i, itemStack.clone());
                });
    }


    public void setItem(int from, int to, ItemStack itemStack, Integer... skippedSlots) {
        Set<Integer> skippedSlotsSet = new HashSet<>(Arrays.asList(skippedSlots));
        IntStream.rangeClosed(from, to)
                .filter(i -> !skippedSlotsSet.contains(i))
                .forEach(i -> {
                    this.inv.setItem(i, itemStack.clone());
                });
    }

    /**
     * This method gathers all inventory slots starting from "from" param skipped by all "skippedSlots" slots.
     * @param from - From which slot it should start.
     * @param itemStack - The item which will be set as a blank at the slots positions.
     * @param skippedSlots - These are skipped slots that are excluded from the array.
     */
    public void setItem(int from, ItemStack itemStack, Integer... skippedSlots) {
        setItem(from, getInventory().getSize()-1, itemStack, skippedSlots);
    }

    /**
     * Sets the cancellation policy for inventory slots.
     *
     * @param policy The cancellation policy to set.
     */
    public void cancellationPolicy(CancellationPolicy policy) {
        this.slots = policy;
    }

    /**
     * Cancel player inventory slots of type Slots.
     */
    public void cancellationPolicyPlayer(CancellationPolicy policy) {
        this.playerSlots = policy;
    }

    public void cancelDragItems(boolean cancel) {
        this.cancelDrag = cancel;
    }

    public void cancelDelayMessage(boolean cancel) {
        this.displayDelayMsg = !cancel;
    }

    /**
     * Checks if the player is currently in the inventory.
     * @param player - Player which is being checked.
     * @return - If the player is in the current BossInventory.
     */
    public boolean isInInventory(Player player) {
        return this.viewers.contains(player.getUniqueId());
    }

    /**
     * Registers a click event handler for a range of inventory slots.
     *
     * @param from     The starting slot index to register the event for.
     * @param to       The ending slot index to register the event for.
     * @param function The function to execute on a click event.
     */
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
    public void registerSlot(int slot, Consumer<InventoryClickEvent> function) {
        this.clickEvents.put(slot, function);
        this.registeredSlots.add(slot);
    }

    public void registerPlayerSlot(int slot, Consumer<InventoryClickEvent> function) {
        this.playerClickEvents.put(slot, function);
        this.registeredPlayerSlots.add(slot);
    }

    /**
     * Register the slot with custom event handler.
     * @param slot - Slot to be registered.
     * @param itemStack - ItemStack will be set in the inventory at the slot position.
     * @param function - Function to be executed.
     */
    public void registerSlot(int slot, ItemStack itemStack, Consumer<InventoryClickEvent> function) {
        this.inv.setItem(slot, itemStack);
        registerSlot(slot, function);
    }

    public void registerSlots(int[] slots, ItemStack itemStack, Consumer<InventoryClickEvent> function) {
        for (int slot : slots) {
            registerSlot(slot, itemStack, function);
        }
    }

    public void registerSlots(int from, int to, ItemStack itemStack, Consumer<InventoryClickEvent> function) {
        for (int i = from; i <= to; i++) {
            registerSlot(i, itemStack, function);
        }
    }

    public void registerPlayerSlots(int[] slots, Consumer<InventoryClickEvent> function) {
        for (int slot : slots) {
            registerPlayerSlot(slot, function);
        }
    }

    public void registerPlayerSlots(int from, int to, Consumer<InventoryClickEvent> function) {
        for (int i = from; i <= to; i++) {
            registerPlayerSlot(i, function);
        }
    }

    /**
     * Retrieves all registered slots and their corresponding click event handlers.
     *
     * @return A map of registered slots with their associated click event handlers.
     */
    public Map<Integer, Consumer<InventoryClickEvent>> getRegisteredSlots() {
        return this.clickEvents;
    }

    /**
     * Registers a custom handler to be called upon inventory close events.
     *
     * @param function The function that will be executed on inventory close.
     */
    public void onInventoryClose(Consumer<InventoryCloseEvent> function) {
        this.closeListeners.add(function);
    }

    /**
     * Unregisters a slot's custom event handler from the list of registered slots.
     *
     * @param slot The slot number to unregister.
     */
    public void unregisterSlot(int slot) {
        this.clickEvents.remove(slot);
        this.registeredSlots.remove(slot);
    }

    /**
     * Unregister a slot custom handler from current list of player inventory registered slots.
     * @param slot - Slot to be unregistered.
     */
    public void unregisterPlayerSlot(int slot) {
        this.playerClickEvents.remove(slot);
        this.registeredPlayerSlots.remove(slot);
    }

    /**
     * Unregisters a slot's custom event handler and sets the specified ItemStack to the slot.
     *
     * @param slot      The slot number to unregister.
     * @param itemStack The ItemStack to set at the slot.
     */
    public void unregisterSlot(int slot, ItemStack itemStack) {
        unregisterSlot(slot);
        setSlot(slot, itemStack);
    }

    /**
     * Opens the current BaseInventory instance to the specified player.
     *
     * @param player Varargs array of players to open the inventory for.
     */
    public void open(HumanEntity... player) {
        for (Runnable refreshListener : this.refreshListeners) {
            refreshListener.run();
        }
        for (HumanEntity humanEntity : player) {
            humanEntity.openInventory(this.inv);
            this.registry.trackInventory(this, humanEntity.getUniqueId());
            this.viewers.add(humanEntity.getUniqueId());
        }
    }

    public void openPreviousInventoryIfPresent(HumanEntity viewer) {
        Deque<BaseInventory> viewerInventories = registry.getInventoryTracker().get(viewer.getUniqueId());
        if (viewerInventories != null && !viewerInventories.isEmpty()) {
            viewerInventories.pop();
            BaseInventory poll = viewerInventories.poll();
            if (poll != null)
                poll.open(viewer);
        }
    }

    public void close() {
        this.viewers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(Player::closeInventory);
    }

    public void updateInventory() {
        this.viewers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(Player::updateInventory);
    }

    public void refresh(Runnable runnable) {
        this.refreshListeners.add(runnable);
    }

    public int getPlayerInventorySlots() {
        return 36;
    }

    public @NotNull Inventory getInventory() {
        return this.inv;
    }

    public List<Player> getPlayerViewers() {
        return this.viewers.stream()
                .map(Bukkit::getOfflinePlayer)
                .filter(OfflinePlayer::isOnline)
                .map(OfflinePlayer::getPlayer)
                .toList();
    }

    public Map<Integer, Consumer<InventoryClickEvent>> getClickSlotEvents() {
        return this.clickEvents;
    }

    /**
     * @param from - From which slot it should start.
     * @param to - At which slot it should end.
     * @param skippedSlots - These are skipped slots that are excluded from the array.
     * @return - Returns filtered slots by skippedSlots.
     */
    public int[] getSlots(int from, int to, Integer... skippedSlots) {
        Set<Integer> skippedSlotsSet = new HashSet<>(Arrays.asList(skippedSlots));
        return IntStream.rangeClosed(from, to)
                .filter(i -> !skippedSlotsSet.contains(i))
                .toArray();
    }

    public boolean isInAnotherInventory(Player player) {
        return this.registry.getLiveInventories()
                .stream()
                .anyMatch(openedInventory -> !isInInventory(player) && openedInventory.isInInventory(player));
    }

    /**
     * Destroys the current inventory instance, unregistering listeners and closing the inventory for all viewers.
     */
    public void destroy() {
        this.close();
        this.unregisterListener();
        for (BukkitTask task : this.tasks.values()) {
            task.cancel();
        }
        this.tasks.clear();
        this.registry.removeLiveInventory(BaseInventory.this);
    }

    /**
     * InventoryListener is a nested class that handles various inventory-related events.
     */
    public class InventoryListener implements Listener {

        @EventHandler
        public void onInventoryClick(InventoryClickEvent e) {
            Player player = (Player) e.getWhoClicked();
            boolean inInventory = isInInventory(player);
            boolean inAnotherInventory = isInAnotherInventory(player);
            if (!inInventory) return;
            if (inAnotherInventory) return;
            handleClick(e);
        }

        @EventHandler
        public void onItemDrag(InventoryDragEvent e) {
            Player player = (Player) e.getWhoClicked();
            if (!isInInventory(player)) return;
            handleDrag(e);
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void onInventoryClose(InventoryCloseEvent e) {
            Player player = (Player) e.getPlayer();
            if (!isInInventory(player)) return;
            handleClose(e);
        }

        @EventHandler(priority = EventPriority.MONITOR)
        public void onInventoryClosePersistent(InventoryCloseEvent e) {
            handleCloseForAnyInventory(e);
        }

    }

    /**
     * CancellationPolicy defines the various cancellation behaviors for inventory events.
     */
    public enum CancellationPolicy {
        ALL,
        REGISTERED,
        UNREGISTERED,
        NONE
    }

}