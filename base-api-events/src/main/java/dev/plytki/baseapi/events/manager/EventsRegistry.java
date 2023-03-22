package dev.plytki.baseapi.events.manager;

import dev.plytki.baseapi.events.exception.UnregisteredListenerException;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.plugin.Plugin;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class EventsRegistry {

    private final Plugin plugin;
    private final Set<Listener> registeredListeners;

    public EventsRegistry(Plugin plugin) {
        this.plugin = plugin;
        this.registeredListeners = new HashSet<>();
    }


    public void register(Listener listener) {
        this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
        this.registeredListeners.add(listener);
    }

    public void registerAll(Listener... listeners) {
        for (Listener listener : listeners) {
            this.plugin.getServer().getPluginManager().registerEvents(listener, this.plugin);
        }
        this.registeredListeners.addAll(Arrays.asList(listeners));
    }

    private Set<Listener> getListeners(Class<? extends Listener> listener) {
        Set<Listener> listeners = new HashSet<>();
        for (Listener registeredListener : this.registeredListeners) {
            if (registeredListener.getClass().equals(listener))
                listeners.add(registeredListener);
        }
        return listeners;
    }

    public void unregister(Class<? extends Listener> listenerClass) throws UnregisteredListenerException {
        for (Listener listener : getListeners(listenerClass)) {
            if (listener == null)
                throw new UnregisteredListenerException(listenerClass.getSimpleName() + " is not registered");
            HandlerList.unregisterAll(listener);
            this.registeredListeners.remove(listener);
        }
    }

    public void unregisterAll() {
        for (Listener registeredListener : this.registeredListeners) {
            HandlerList.unregisterAll(registeredListener);
        }
        this.registeredListeners.clear();
    }

}