package dev.plytki.baseapi.inventories.model.task;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

public class BaseTaskManager {

    private final Plugin plugin;
    private final Map<Long, BukkitTask> tasks;
    private long tasksCounter;

    public BaseTaskManager(Plugin plugin) {
        this.plugin = plugin;
        this.tasks = new HashMap<>();
        this.tasksCounter = 0;
    }

    public BukkitTask getTask(long taskId) {
        return tasks.get(taskId);
    }

    public long runTask(Runnable runnable, long delay) {
        long taskId = ++tasksCounter;
        this.tasks.put(taskId, scheduleTask(runnable, delay, 0));
        return taskId;
    }

    public long runTask(long taskId, Runnable runnable, long delay) {
        stopTask(taskId);
        this.tasks.put(taskId, scheduleTask(runnable, delay, 0));
        return taskId;
    }

    public long runTask(Runnable runnable, long delay, long period) {
        long taskId = ++tasksCounter;
        this.tasks.put(taskId, scheduleTask(runnable, delay, period));
        return taskId;
    }

    public long runTask(long taskId, Runnable runnable, long delay, long period) {
        stopTask(taskId);
        this.tasks.put(taskId, scheduleTask(runnable, delay, period));
        return taskId;
    }

    public boolean stopTask(long taskId) {
        BukkitTask bukkitTask = this.tasks.remove(taskId);
        if (bukkitTask != null) {
            bukkitTask.cancel();
            return true;
        }
        return false;
    }

    public void stopAllTasks() {
        for (BukkitTask value : this.tasks.values()) {
            value.cancel();
        }
        this.tasks.clear();
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

}
