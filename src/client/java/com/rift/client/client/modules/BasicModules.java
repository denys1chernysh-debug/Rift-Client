package com.rift.client.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

/**
 * RIFT's module registry. Only the modules from the RIFT HTML design are listed here.
 * A module is marked implemented only when it has real in-game behavior.
 */
public final class BasicModules {
    private static boolean registered;
    private BasicModules() {}

    public static void register() {
        if (registered) return;
        registered = true;

        // Combat — kept visible in the GUI, but automated combat is intentionally unavailable.
        add("Aim Assist", "Combat automation is not available in this build.", "Combat", false);
        add("Triggerbot", "Automated attacking is not available in this build.", "Combat", false);
        add("Auto Crystal", "Automated combat actions are not available in this build.", "Combat", false);
        add("Anchor Macro", "Automated combat macros are not available in this build.", "Combat", false);
        add("Auto Mace", "Automated combat actions are not available in this build.", "Combat", false);

        // Player
        ModuleManager.register(new Module("Sprint", "Keeps sprint active while you move.", "Player") {
            @Override public void onTick() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null && player.input != null && player.isAlive()) {
                    player.setSprinting(true);
                }
            }
            @Override public void onDisable() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) player.setSprinting(false);
            }
        });
        add("Auto Tool", "Automatic tool selection is not implemented yet.", "Player", false);
        add("Auto Pot", "Automated potion use is not available in this build.", "Player", false);
        add("Bridge Assist", "Placement assistance is not implemented yet.", "Player", false);
        add("Auto Jump Reset", "Automated movement timing is not available in this build.", "Player", false);

        // Render — the two ESP modules inspect client-loaded data and use normal depth testing.
        ModuleManager.register(new Module("Player ESP", "Outline players currently present in your loaded world.", "Render") {});
        ModuleManager.register(new Module("Storage ESP", "Highlight storage blocks in chunks already loaded by your client.", "Render") {});
        add("Trajectories", "Projectile path prediction is not implemented yet.", "Render", false);
        add("Freecam", "Independent camera movement is not implemented yet.", "Render", false);

        // Donut
        add("Sus Chunks", "Chunk analysis is not implemented yet.", "Donut", false);
        add("Base Finder", "Base detection is not implemented yet.", "Donut", false);
        add("Amethyst Finder", "Amethyst scanning is not implemented yet.", "Donut", false);
        add("Ancient Debris Finder", "Ancient debris scanning is not implemented yet.", "Donut", false);

        // Misc
        add("Auto XP", "Automated item use is not available in this build.", "Misc", false);
        add("Pearl Macro", "Automated item-use macros are not available in this build.", "Misc", false);
        add("Fast Place", "Placement automation is not available in this build.", "Misc", false);

        // Client
        add("Click GUI", "Open and close the RIFT menu with Right Shift.", "Client", true);
        add("Anti-Bot", "Bot filtering is not implemented yet.", "Client", false);
        add("Friends List", "A persistent friends manager is not implemented yet.", "Client", false);
        add("Self-Destruct", "Self-removal is not included in this build.", "Client", false);
    }

    private static void add(String name, String description, String category, boolean implemented) {
        ModuleManager.register(new Module(name, description, category, implemented) {});
    }
}
