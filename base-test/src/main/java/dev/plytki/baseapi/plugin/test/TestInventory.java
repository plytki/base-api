package dev.plytki.baseapi.plugin.test;

import dev.plytki.baseapi.inventories.manager.InventoryRegistry;
import dev.plytki.baseapi.inventories.model.inventory.BaseInventory;
import dev.plytki.baseapi.inventories.util.ItemBuilder;
import dev.plytki.baseapi.plugin.TestPlugin;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;

public class TestInventory extends BaseInventory {


    public TestInventory(InventoryRegistry inventoryRegistry) {
        super(inventoryRegistry, "Test Inventory", 6);
        cancellationPolicy(CancellationPolicy.ALL);

        registerTask(() -> {
            System.out.println("one time task");
            System.out.println("test 1");
        }, 10, 0);

        registerTask(() -> {
            System.out.println("repeating task");
            System.out.println("test 2");
        }, 10, 0);

        animateBorder(Material.GREEN_WOOL, 2);

        registerSlot(1, new ItemBuilder(Material.GREEN_WOOL).toItemStack(), event -> {
            event.getWhoClicked().sendMessage("§dYou clicked slot 1!");
        });
        setItem(9, 10, new ItemStack(Material.BOOK));
    }


    public void animateBorder(Material borderMaterial, long intervalTicks) {
        // Define the animation steps
        int height = 6;
        int width = 9;

        int[][] inventorySlots = new int[height][width];
        int count = 0;
        for (int i = 0; i < height; i++) {
            for (int j = 0; j < width; j++) {
                inventorySlots[i][j] = count++;
            }
        }

        List<Runnable> animationSteps = new ArrayList<>();

        // Top row animation
        for (int col = 0; col < width; col++) {
            int slot = inventorySlots[0][col];
            animationSteps.add(() -> setItem(slot, new ItemStack(borderMaterial)));
        }

        // Right border animation
        for (int row = 1; row < height; row++) {
            int slot = inventorySlots[row][width - 1];
            animationSteps.add(() -> setItem(slot, new ItemStack(borderMaterial)));
        }

        // Bottom row animation (reversed)
        for (int col = width - 1; col >= 0; col--) {
            int slot = inventorySlots[height - 1][col];
            animationSteps.add(() -> setItem(slot, new ItemStack(borderMaterial)));
        }

        // Left border animation (reversed)
        for (int row = height - 2; row >= 0; row--) {
            int slot = inventorySlots[row][0];
            animationSteps.add(() -> setItem(slot, new ItemStack(borderMaterial)));
        }

        // Create a repeating task to perform the animation steps
        int taskId = 605;
        registerTask(taskId, new Runnable() {
            private int currentStep = 0;
            @Override
            public void run() {
                if (currentStep < animationSteps.size()) {
                    // Run the current animation step
                    animationSteps.get(currentStep).run();
                    currentStep++;
                } else {
                    // Animation is complete, cancel the task
                    cancelTask(taskId);
                }
            }
        }, 0L, intervalTicks);
    }

}