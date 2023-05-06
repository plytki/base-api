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

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.IntStream;

public abstract class BaseInventory implements IBaseInventory, InventoryHolder {

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

    private Slots cancelledSlots;
    private Slots cancelledPlayerSlots;
    private boolean cancelDrag;
    private final Map<Long, BukkitTask> tasks = new HashMap<>();
    private final List<Integer> blankSlots = new ArrayList<>();

    private final Map<UUID, Long> delayMap = new HashMap<>();
    private long clickDelay = 0L;
    private boolean displayDelayMsg;
    private String delayMsg;
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private boolean persistent;

    public BaseInventory(InventoryRegistry inventoryRegistry, String inventoryName, int inventoryLines, Player... viewers) {
        this.inv = Bukkit.createInventory(null, inventoryLines * 9, Component.text(inventoryName));
        this.registry = inventoryRegistry;
        this.listener = new InventoryListener();
        this.plugin = inventoryRegistry.getPlugin();
        this.cancelledSlots = Slots.NONE;
        this.cancelledPlayerSlots = Slots.NONE;
        this.displayDelayMsg = true;
        setupMessages();
        registerListener();
        this.registry.addLiveInventory(this);
        this.persistent = false;
        open(viewers);
    }

    private void registerListener() {
        this.plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }

    private void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        this.clickDebug.forEach(consumer -> consumer.accept(e));

        boolean isPlayerInventory = e.getClickedInventory() instanceof PlayerInventory;
        Slots cancelledSlots = isPlayerInventory ? this.cancelledPlayerSlots : this.cancelledSlots;
        Set<Integer> registeredSlots = isPlayerInventory ? this.registeredPlayerSlots : this.registeredSlots;
        Map<Integer, Consumer<InventoryClickEvent>> clickSlotEvents = isPlayerInventory ? this.playerClickEvents : this.clickEvents;

        if (registeredSlots.contains(e.getSlot())) {
            if (cancelledSlots == Slots.REGISTERED || cancelledSlots == Slots.ALL) {
                e.setCancelled(true);
            }
        } else if (cancelledSlots == Slots.UNREGISTERED || cancelledSlots == Slots.ALL) {
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
            this.closeListeners.forEach(onCloseListener -> onCloseListener.accept(e));
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent e) {
        Player player = (Player) e.getPlayer();
        if (!isInInventory(player)) return;
        handleClose(e);
    }

    @EventHandler
    public void onInventoryClosePersistent(InventoryCloseEvent e) {
        handleCloseForAnyInventory(e);
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
                        this.blankSlots.add(i);
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

    public long getClickDelay() {
        return this.clickDelay;
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
        this.tasks.put(taskID, new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskTimer(this.plugin, delay, period));
    }

    public void registerTask(Runnable runnable, long delay, long period) {
        registerTask(new Random().nextLong(), runnable, delay, period);
    }

    public void registerTask(long taskID, Runnable runnable, long delay) {
        cancelTask(taskID);
        this.tasks.put(taskID, new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskLater(this.plugin, delay));
    }

    public void registerTask(Runnable runnable, long delay) {
        registerTask(new Random().nextLong(), runnable, delay);
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

    public void setBlank(int from, int to, ItemStack itemStack, Integer... skippedSlots) {
        Set<Integer> skippedSlotsSet = new HashSet<>(Arrays.asList(skippedSlots));
        boolean isAir = itemStack.getType() == Material.AIR;
        IntStream.rangeClosed(from, to)
                .filter(i -> !skippedSlotsSet.contains(i))
                .forEach(i -> {
                    this.inv.setItem(i, itemStack.clone());
                    if (isAir) {
                        blankSlots.add(i);
                    }
                });
    }

    /**
     * This method gathers all inventory slots starting from "from" param skipped by all "skippedSlots" slots.
     * @param from - From which slot it should start.
     * @param itemStack - The item which will be set as a blank at the slots positions.
     * @param skippedSlots - These are skipped slots that are excluded from the array.
     */
    public void setBlank(int from, ItemStack itemStack, Integer... skippedSlots) {
        setBlank(from, getInventory().getSize()-1, itemStack, skippedSlots);
    }

    /**
     * Cancel inventory slots of type Slots.
     */
    public void cancel(Slots slots) {
        this.cancelledSlots = slots;
    }

    /**
     * Cancel player inventory slots of type Slots.
     */
    public void cancelPlayer(Slots slots) {
        this.cancelledPlayerSlots = slots;
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
     * Register slots with custom event handler.
     * @param from - Starting slot.
     * @param to - Ending slot.
     * @param function - Function that will be executed.
     */
    public void registerSlots(int from, int to, Consumer<InventoryClickEvent> function) {
        for (int i = from; i <= to; i++) {
            registerSlot(i, function);
        }
    }

    /**
     * Register slots with custom event handler.
     * @param slots - Slots to register.
     * @param function - Function that will be executed.
     */
    public void registerSlots(int[] slots, Consumer<InventoryClickEvent> function) {
        for (int slot : slots) {
            registerSlot(slot, function);
        }
    }

    /**
     * Register the slot with custom event handler.
     * @param slot - Slot that will be registered.
     * @param function - Function that will be executed.
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
     * @return All registered slots.
     */
    public Map<Integer, Consumer<InventoryClickEvent>> getRegisteredSlots() {
        return this.clickEvents;
    }

    /**
     * Register a custom handler on inventory close.
     * @param function - Function that will be called on InventoryCloseEvent.
     */
    public void onInventoryClose(Consumer<InventoryCloseEvent> function) {
        this.closeListeners.add(function);
    }

    /**
     * Unregister a slot custom handler from current list of registered slots.
     * @param slot - Slot to be unregistered.
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
     * Unregister a slot custom handler from current list of registered slots
     * @param slot - Slot to be unregistered.
     * @param itemStack - ItemStack that will be set at the provided slot index.
     */
    public void unregisterSlot(int slot, ItemStack itemStack) {
        unregisterSlot(slot);
        setSlot(slot, itemStack);
    }

    /**
     * Open an instance of BossInventory to a player (The only proper way to open)
     * @param player - Player that the inventory will be opened for.
     */
    public void open(HumanEntity... player) {
        for (Runnable refreshListener : this.refreshListeners) {
            refreshListener.run();
        }
        for (HumanEntity humanEntity : player) {
            humanEntity.openInventory(this.inv);
            this.viewers.add(humanEntity.getUniqueId());
        }
    }

    public void close() {
        this.viewers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(Player::closeInventory);
    }

    public void updateInventory() {
        this.viewers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(Player::updateInventory);
    }

    public void refresh(Runnable runnable) {
        this.refreshListeners.add(runnable);
    }

    public int getPlayerInventorySlots() {
        return 36;
    }

    public Inventory getInventory() {
        return this.inv;
    }

    public Set<UUID> getViewers() {
        return this.viewers;
    }

    public List<Player> getPlayerViewers() {
        return this.viewers.stream().map(Bukkit::getOfflinePlayer).filter(OfflinePlayer::isOnline).map(OfflinePlayer::getPlayer).toList();
    }

    public Map<Integer, Consumer<InventoryClickEvent>> getClickSlotEvents() {
        return this.clickEvents;
    }

    public List<Integer> getBlankSlots() {
        return this.blankSlots;
    }

    public Map<UUID, Long> getDelayMap() {
        return this.delayMap;
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

    public void destroy() {
        this.close();
        this.unregisterListener();
        for (BukkitTask task : this.tasks.values()) {
            task.cancel();
        }
        this.tasks.clear();
        this.registry.removeLiveInventory(BaseInventory.this);
    }

    public Plugin getPlugin() {
        return this.plugin;
    }

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

    public enum Slots {

        ALL,
        REGISTERED,
        UNREGISTERED,
        NONE

    }

}