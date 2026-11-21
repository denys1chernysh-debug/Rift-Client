package com.rift.client.client.modules;

import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * RIFT module registry. Modules stay disabled in the GUI until their behavior
 * is implemented; this avoids presenting empty toggles as working features.
 */
public final class BasicModules {
    private static boolean registered;
    private BasicModules() {}

    public static void register() {
        if (registered) return;
        registered = true;

        // Combat
        add("Aim Assist", "Smoothly adjusts aim toward a target.", "Combat", false);
        add("Triggerbot", "Attacks when a valid target is under the crosshair.", "Combat", false);
        add("Auto Crystal", "Crystal placement and detonation helper.", "Combat", false);
        add("Anchor Macro", "Respawn-anchor charge and detonation helper.", "Combat", false);
        add("Auto Mace", "Mace swap timing helper.", "Combat", false);

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

        ModuleManager.register(new Module("Auto Tool", "Selects the hotbar tool with the best destroy speed for the targeted block.", "Player") {
            @Override public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null || mc.level == null || !(mc.hitResult instanceof BlockHitResult hit)) return;
                BlockPos pos = hit.getBlockPos();
                BlockState state = mc.level.getBlockState(pos);
                int bestSlot = player.getInventory().selected;
                float bestSpeed = -1.0f;
                for (int slot = 0; slot < 9; slot++) {
                    ItemStack stack = player.getInventory().getItem(slot);
                    float speed;
                    try { speed = stack.getDestroySpeed(state); }
                    catch (Throwable ignored) { speed = 0.0f; }
                    if (speed > bestSpeed) {
                        bestSpeed = speed;
                        bestSlot = slot;
                    }
                }
                player.getInventory().selected = bestSlot;
            }
        });

        ModuleManager.register(new Module("Auto Pot", "Uses a healing or regeneration potion when health is low.", "Player") {
            private int previousSlot = -1;
            private int restoreTicks;
            @Override public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null || mc.gameMode == null) return;
                if (restoreTicks > 0) {
                    restoreTicks--;
                    if (restoreTicks == 0 && previousSlot >= 0) {
                        player.getInventory().selected = previousSlot;
                        previousSlot = -1;
                    }
                    return;
                }
                if (player.getHealth() > 8.0f) return;
                int potionSlot = -1;
                for (int slot = 0; slot < 9; slot++) {
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!(stack.getItem() instanceof PotionItem)) continue;
                    String name = stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT);
                    if (name.contains("healing") || name.contains("regeneration")) {
                        potionSlot = slot;
                        break;
                    }
                }
                if (potionSlot < 0) return;
                previousSlot = player.getInventory().selected;
                player.getInventory().selected = potionSlot;
                mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                restoreTicks = 2;
            }
            @Override public void onDisable() {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player != null && previousSlot >= 0) mc.player.getInventory().selected = previousSlot;
                previousSlot = -1;
                restoreTicks = 0;
            }
        });

        add("Bridge Assist", "Helps time safe sneaking while bridging.", "Player", false);
        ModuleManager.register(new Module("Auto Jump Reset", "Jumps when the player is taking damage to reset sprint knockback where supported.", "Player") {
            @Override public void onTick() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null || !player.isAlive() || !player.onGround()) return;
                if (readInt(player, "hurtTime") > 0) player.jump();
            }
        });

        // Render
        ModuleManager.register(new Module("Player ESP", "Outlines players currently present in your loaded world.", "Render") {});
        ModuleManager.register(new Module("Storage ESP", "Highlights storage blocks in chunks already loaded by your client.", "Render") {});
        add("Trajectories", "Draws predicted paths for supported projectiles.", "Render", false);
        add("Freecam", "Detaches the camera from the local player.", "Render", false);

        // Donut
        add("Sus Chunks", "Scores suspicious patterns in chunks already loaded by the client.", "Donut", false);
        add("Base Finder", "Combines visible-world scanners to flag possible player-built areas.", "Donut", false);
        add("Amethyst Finder", "Finds visible amethyst geodes and openings in loaded chunks.", "Donut", false);
        add("Ancient Debris Finder", "Finds ancient debris in blocks already sent to the client.", "Donut", false);

        // Misc
        ModuleManager.register(new Module("Auto XP", "Automatically throws experience bottles from the hotbar.", "Misc") {
            @Override public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null || mc.gameMode == null) return;
                ItemStack held = player.getMainHandItem();
                if (held.is(Items.EXPERIENCE_BOTTLE)) {
                    mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                }
            }
        });
        add("Pearl Macro", "Switches to an ender pearl, uses it, then restores the previous slot.", "Misc", false);
        add("Fast Place", "Reduces the delay between block placement actions.", "Misc", false);

        // Client
        add("Click GUI", "Open and close the RIFT menu with Right Shift.", "Client", true);
        add("Anti-Bot", "Filters likely fake player entities from client-side displays.", "Client", false);
        add("Friends List", "Stores teammate names and identifies them in client displays.", "Client", false);
        add("Self-Destruct", "Disables RIFT modules and hides its overlays for this session.", "Client", false);
    }

    private static int readInt(Object target, String name) {
        for (Class<?> type = target.getClass(); type != null; type = type.getSuperclass()) {
            try {
                Field field = type.getDeclaredField(name);
                field.setAccessible(true);
                return field.getInt(target);
            } catch (ReflectiveOperationException | RuntimeException ignored) {}
        }
        return 0;
    }

    private static void add(String name, String description, String category, boolean implemented) {
        ModuleManager.register(new Module(name, description, category, implemented) {});
    }
}
