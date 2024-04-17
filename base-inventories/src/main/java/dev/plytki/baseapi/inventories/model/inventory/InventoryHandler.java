package dev.plytki.baseapi.inventories.model.inventory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.Plugin;

import java.util.*;
import java.util.function.Consumer;

@Getter
public class InventoryHandler {

    private final BaseInventory baseInventory;
    private final Listener listener;

    private final Set<UUID> viewers;
    private final Set<Consumer<InventoryClickEvent>> clickDebug;
    private final Map<Integer, Consumer<InventoryClickEvent>> clickEvents;
    private final Map<Integer, Consumer<InventoryClickEvent>> playerClickEvents;

    private final Set<Integer> registeredSlots;
    private final Set<Integer> registeredPlayerSlots;

    private final Set<Consumer<InventoryCloseEvent>> closeListeners;

    private CancelPolicy slots;
    private CancelPolicy playerSlots;
    private boolean cancelDrag;

    private final Map<UUID, Long> cooldownMap = new HashMap<>();

    @Setter
    @Getter
    private long clickCooldown = 0L;
    @Getter
    private boolean displayCooldownMessage;
    private String delayMsg;
    @Getter(AccessLevel.PROTECTED)
    @Setter(AccessLevel.PROTECTED)
    private boolean persistent;

    public InventoryHandler(BaseInventory baseInventory, Plugin plugin) {
        this.baseInventory = baseInventory;
        this.listener = new InventoryListener();

        this.viewers = new HashSet<>();
        this.clickDebug = new HashSet<>();
        this.clickEvents = new HashMap<>();
        this.playerClickEvents = new HashMap<>();
        this.registeredSlots = new HashSet<>();
        this.registeredPlayerSlots = new HashSet<>();
        this.closeListeners = new HashSet<>();
        this.slots = CancelPolicy.NONE;
        this.playerSlots = CancelPolicy.NONE;
        this.displayCooldownMessage = true;
        this.persistent = false;

        plugin.getServer().getPluginManager().registerEvents(this.listener, plugin);
    }

    /**
     * Sets the cancellation policy for inventory slots.
     *
     * @param policy The cancellation policy to set.
     */
    public void cancelPolicy(CancelPolicy policy) {
        this.slots = policy;
    }

    /**
     * Cancel player inventory slots of type Slots.
     */
    public void cancelPolicyPlayer(CancelPolicy policy) {
        this.playerSlots = policy;
    }


    public void setItemsDrag(boolean enabled) {
        this.cancelDrag = !enabled;
    }

    public void setCooldownMessage(boolean enabled) {
        this.displayCooldownMessage = enabled;
    }

    private void updateClickCooldown(Player player) {
        this.cooldownMap.put(player.getUniqueId(), System.currentTimeMillis());
    }

    protected boolean hasClickCooldown(Player player) {
        return Optional.ofNullable(this.cooldownMap.get(player.getUniqueId()))
                .map(delay -> delay + this.clickCooldown >= System.currentTimeMillis())
                .orElse(false);
    }

    private long getDelay(Player player) {
        return this.cooldownMap.getOrDefault(player.getUniqueId(), 0L);
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

    private void handleClick(InventoryClickEvent e) {
        Player player = (Player) e.getWhoClicked();
        this.clickDebug.forEach(consumer -> consumer.accept(e));

        boolean isPlayerInventory = e.getClickedInventory() instanceof PlayerInventory;
        CancelPolicy slotsPolicy = isPlayerInventory ? this.playerSlots : this.slots;
        Set<Integer> registeredSlots = isPlayerInventory ? this.registeredPlayerSlots : this.registeredSlots;
        Map<Integer, Consumer<InventoryClickEvent>> clickSlotEvents = isPlayerInventory ? this.playerClickEvents : this.clickEvents;

        if (registeredSlots.contains(e.getSlot())) {
            if (slotsPolicy == CancelPolicy.REGISTERED || slotsPolicy == CancelPolicy.ALL) {
                e.setCancelled(true);
            }
        } else if (slotsPolicy == CancelPolicy.UNREGISTERED || slotsPolicy == CancelPolicy.ALL) {
            e.setCancelled(true);
        }

        clickSlotEvents.entrySet().stream()
                .filter(entry -> e.getSlot() == entry.getKey())
                .findFirst()
                .ifPresent(entry -> {
                    if (hasClickCooldown(player)) {
                        e.setCancelled(true);
                        if (displayCooldownMessage) {
                            player.sendMessage(String.format(delayMsg, getDelay(player) + this.clickCooldown - System.currentTimeMillis()));
                        }
                    } else {
                        updateClickCooldown(player);
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
            Deque<BaseInventory> baseInventories = this.baseInventory.getRegistry().getInventoryTracker().get(e.getPlayer().getUniqueId());
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

    protected void debug(Consumer<InventoryClickEvent> eventConsumer) {
        this.clickDebug.add(eventConsumer);
    }

    public boolean isInInventory(Player player) {
        return this.viewers.contains(player.getUniqueId());
    }

    public boolean isInAnotherInventory(Player player) {
        return baseInventory.getRegistry().getLiveInventories()
                .stream()
                .anyMatch(openedInventory -> !isInInventory(player) && openedInventory.isInInventory(player));
    }

    public void updateInventory() {
        this.viewers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(Player::updateInventory);
    }

    protected void close() {
        this.viewers.stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .forEach(Player::closeInventory);
    }

    /**
     * Destroys the current inventory instance, unregistering listeners and closing the inventory for all viewers.
     */
    public void destroy() {
        this.close();
        this.unregisterListener();
        this.baseInventory.getTaskManager().stopAllTasks();
        this.baseInventory.getRegistry().removeLiveInventory(baseInventory);
    }

    /**
     * InventoryListener is a nested class that handles various inventory-related events.
     */
    private final class InventoryListener implements Listener {

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

    public void registerSlot(int slot, Consumer<InventoryClickEvent> function) {
        this.clickEvents.put(slot, function);
        this.registeredSlots.add(slot);
    }

    public void registerPlayerSlot(int slot, Consumer<InventoryClickEvent> function) {
        this.playerClickEvents.put(slot, function);
        this.registeredPlayerSlots.add(slot);
    }

    public void unregisterSlot(int slot) {
        this.clickEvents.remove(slot);
        this.registeredSlots.remove(slot);
    }

    public void unregisterPlayerSlot(int slot) {
        this.playerClickEvents.remove(slot);
        this.registeredPlayerSlots.remove(slot);
    }

}
