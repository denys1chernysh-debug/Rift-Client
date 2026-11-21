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
        ModuleManager.register(new Module("Aim Assist", "Gently nudges your view toward the nearest player already close to your crosshair.", "Combat") {
            @Override public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null || mc.level == null || !player.isAlive()) return;
                net.minecraft.world.entity.player.Player best = null;
                double bestDistance = 4.5 * 4.5;
                var look = player.getLookAngle();
                for (var target : mc.level.players()) {
                    if (target == player || !target.isAlive() || target.isRemoved()) continue;
                    double distance = player.distanceToSqr(target);
                    if (distance > bestDistance) continue;
                    var direction = target.getBoundingBox().getCenter().subtract(player.getEyePosition()).normalize();
                    if (look.dot(direction) < 0.78) continue;
                    best = target;
                    bestDistance = distance;
                }
                if (best == null) return;
                var delta = best.getBoundingBox().getCenter().subtract(player.getEyePosition());
                double horizontal = Math.sqrt(delta.x * delta.x + delta.z * delta.z);
                float yaw = (float)(Math.toDegrees(Math.atan2(delta.z, delta.x)) - 90.0);
                float pitch = (float)-Math.toDegrees(Math.atan2(delta.y, horizontal));
                player.setYRot(player.getYRot() + wrapDegrees(yaw - player.getYRot()) * 0.12f);
                player.setXRot(player.getXRot() + (pitch - player.getXRot()) * 0.12f);
            }
        });
        ModuleManager.register(new Module("Triggerbot", "Attacks a living player only when they are directly under your crosshair and in normal reach.", "Combat") {
            @Override public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null || mc.gameMode == null || !player.isAlive()
                    || !(mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit)) return;
                if (!(hit.getEntity() instanceof net.minecraft.world.entity.player.Player target)
                    || target == player || !target.isAlive() || player.distanceToSqr(target) > 3.0 * 3.0) return;
                if (player.getAttackStrengthScale(0.5f) >= 1.0f) {
                    mc.gameMode.attack(player, target);
                    player.swing(InteractionHand.MAIN_HAND);
                }
            }
        });
        add("Auto Crystal", "Crystal placement and detonation helper.", "Combat", false);
        add("Anchor Macro", "Respawn-anchor charge and detonation helper.", "Combat", false);
        ModuleManager.register(new Module("Auto Mace", "Selects a hotbar mace during a falling attack and restores your previous slot after landing.", "Combat") {
            private int previousSlot = -1;
            @Override public void onTick() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player == null || !player.isAlive()) return;
                if (player.getDeltaMovement().y < -0.08 && player.fallDistance > 2.0f) {
                    for (int slot = 0; slot < 9; slot++) {
                        if (player.getInventory().getItem(slot).is(Items.MACE)) {
                            if (previousSlot < 0) previousSlot = player.getInventory().selected;
                            player.getInventory().selected = slot;
                            return;
                        }
                    }
                } else if (player.onGround() && previousSlot >= 0) {
                    player.getInventory().selected = previousSlot;
                    previousSlot = -1;
                }
            }
            @Override public void onDisable() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null && previousSlot >= 0) player.getInventory().selected = previousSlot;
                previousSlot = -1;
            }
        });

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

        ModuleManager.register(new Module("Auto Pot", "Uses healing or regeneration potions from the hotbar or main inventory when health is low, then refills the used hotbar slot.", "Player") {
            private int previousSlot = -1;
            private int restoreTicks;
            private int useCooldown;

            @Override public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null || mc.gameMode == null || !player.isAlive()) return;

                if (restoreTicks > 0 && --restoreTicks == 0 && previousSlot >= 0) {
                    player.getInventory().selected = previousSlot;
                    previousSlot = -1;
                }
                if (useCooldown > 0) useCooldown--;
                if (restoreTicks > 0 || useCooldown > 0 || player.getHealth() > 8.0f) return;

                int hotbarSlot = findPotion(player, 0, 9);
                if (hotbarSlot < 0) {
                    int inventorySlot = findPotion(player, 9, 36);
                    if (inventorySlot < 0) return;
                    hotbarSlot = player.getInventory().selected;
                    mc.gameMode.handleInventoryMouseClick(
                        player.containerMenu.containerId, inventorySlot, hotbarSlot,
                        net.minecraft.world.inventory.ClickType.SWAP, player
                    );
                }

                previousSlot = player.getInventory().selected;
                player.getInventory().selected = hotbarSlot;
                mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                restoreTicks = 2;
                useCooldown = 30;
            }

            private int findPotion(LocalPlayer player, int start, int end) {
                for (int slot = start; slot < end; slot++) {
                    ItemStack stack = player.getInventory().getItem(slot);
                    if (!(stack.getItem() instanceof PotionItem)) continue;
                    String name = stack.getHoverName().getString().toLowerCase(java.util.Locale.ROOT);
                    if (name.contains("healing") || name.contains("instant health")
                        || name.contains("regeneration")) return slot;
                }
                return -1;
            }

            @Override public void onDisable() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null && previousSlot >= 0) player.getInventory().selected = previousSlot;
                previousSlot = -1;
                restoreTicks = 0;
                useCooldown = 0;
            }
        });

        ModuleManager.register(new Module("Bridge Assist", "Automatically sneaks when your movement would carry you over an unsupported edge.", "Player") {
            @Override public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null || !player.isAlive() || !player.onGround() || player.input == null) return;
                double dx = player.getDeltaMovement().x;
                double dz = player.getDeltaMovement().z;
                if (Math.abs(dx) + Math.abs(dz) < 0.015) {
                    player.setShiftKeyDown(false);
                    return;
                }
                double lead = 0.55;
                BlockPos landing = BlockPos.containing(player.getX() + dx * lead,
                    player.getY() - 1.0, player.getZ() + dz * lead);
                boolean edge = mc.level != null && mc.level.getBlockState(landing).isAir();
                player.setShiftKeyDown(edge || mc.options.keyShift.isDown());
            }
            @Override public void onDisable() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) player.setShiftKeyDown(false);
            }
        });
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

        // Donut scanners inspect only chunks the server has already sent to this client.
        ModuleManager.register(new Module("Sus Chunks", "Scores loaded chunks using visible player-build and storage block counts.", "Donut") {
            @Override public void onTick() { DonutScanner.tick(DonutScanner.Scan.SUSPICIOUS); }
        });
        ModuleManager.register(new Module("Base Finder", "Combines visible storage, utility, and player-build block signals in loaded chunks.", "Donut") {
            @Override public void onTick() { DonutScanner.tick(DonutScanner.Scan.BASES); }
        });
        ModuleManager.register(new Module("Amethyst Finder", "Scans loaded chunks for amethyst geode blocks.", "Donut") {
            @Override public void onTick() { DonutScanner.tick(DonutScanner.Scan.AMETHYST); }
        });
        ModuleManager.register(new Module("Ancient Debris Finder", "Scans loaded Nether chunks for ancient debris.", "Donut") {
            @Override public void onTick() { DonutScanner.tick(DonutScanner.Scan.DEBRIS); }
        });

        // Misc
        ModuleManager.register(new Module("Auto XP", "Throws an XP bottle from your hotbar at a controlled interval.", "Misc") {
            private int cooldown;
            private int restoreSlot = -1;
            private int restoreIn;
            @Override public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null || mc.gameMode == null) return;
                if (restoreIn > 0 && --restoreIn == 0 && restoreSlot >= 0) {
                    player.getInventory().selected = restoreSlot;
                    restoreSlot = -1;
                }
                if (cooldown > 0) { cooldown--; return; }
                int bottleSlot = -1;
                for (int slot = 0; slot < 9; slot++) {
                    if (player.getInventory().getItem(slot).is(Items.EXPERIENCE_BOTTLE)) {
                        bottleSlot = slot;
                        break;
                    }
                }
                if (bottleSlot < 0) return;
                if (restoreSlot < 0) restoreSlot = player.getInventory().selected;
                player.getInventory().selected = bottleSlot;
                mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                restoreIn = 2;
                cooldown = 4;
            }
            @Override public void onDisable() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null && restoreSlot >= 0) player.getInventory().selected = restoreSlot;
                restoreSlot = -1;
                restoreIn = 0;
                cooldown = 0;
            }
        });

        ModuleManager.register(new Module("Pearl Macro", "On activation, throws one hotbar ender pearl and restores your selected slot.", "Misc") {
            @Override public void onEnable() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;
                if (player == null || mc.gameMode == null) { setEnabled(false); return; }
                int oldSlot = player.getInventory().selected;
                int pearlSlot = -1;
                for (int slot = 0; slot < 9; slot++) {
                    if (player.getInventory().getItem(slot).is(Items.ENDER_PEARL)) {
                        pearlSlot = slot;
                        break;
                    }
                }
                if (pearlSlot >= 0) {
                    player.getInventory().selected = pearlSlot;
                    mc.gameMode.useItem(player, InteractionHand.MAIN_HAND);
                    player.getInventory().selected = oldSlot;
                }
                setEnabled(false);
            }
        });

        ModuleManager.register(new Module("Fast Place", "Removes the vanilla right-click placement delay when the game exposes that timer.", "Misc") {
            private Field delayField;
            private Object gameMode;
            private int originalDelay = -1;
            @Override public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                if (mc.gameMode == null) return;
                if (gameMode != mc.gameMode) {
                    gameMode = mc.gameMode;
                    delayField = findField(gameMode.getClass(), "rightClickDelay");
                }
                if (delayField == null) return;
                try {
                    delayField.setAccessible(true);
                    if (originalDelay < 0) originalDelay = delayField.getInt(gameMode);
                    delayField.setInt(gameMode, 0);
                } catch (ReflectiveOperationException | RuntimeException ignored) {}
            }
            @Override public void onDisable() {
                if (delayField != null && gameMode != null && originalDelay >= 0) {
                    try { delayField.setAccessible(true); delayField.setInt(gameMode, originalDelay); }
                    catch (ReflectiveOperationException | RuntimeException ignored) {}
                }
                originalDelay = -1;
                gameMode = null;
                delayField = null;
            }
        });

        // Client
        add("Click GUI", "Open and close the RIFT menu with Right Shift.", "Client", true);
        ModuleManager.register(new Module("Anti-Bot", "Filters obvious NPC and non-player fake-name entries from RIFT player overlays.", "Client") {
            @Override public void onTick() { ClientUtilities.refreshAntiBot(); }
        });
        ModuleManager.register(new Module("Friends List", "Adds the player under your crosshair to the persistent RIFT friends list when enabled.", "Client") {
            @Override public void onEnable() {
                Minecraft mc = Minecraft.getInstance();
                if (mc.hitResult instanceof net.minecraft.world.phys.EntityHitResult hit
                    && hit.getEntity() instanceof net.minecraft.world.entity.player.Player target
                    && mc.player != null && target != mc.player) {
                    ClientUtilities.toggleFriend(target.getGameProfile().name());
                }
                setEnabled(false);
            }
        });
        ModuleManager.register(new Module("Self-Destruct", "Immediately disables RIFT modules and closes the RIFT menu for this session.", "Client") {
            @Override public void onEnable() {
                for (Module module : ModuleManager.getModules()) {
                    if (module != this) module.setEnabled(false);
                }
                Minecraft.getInstance().gui.setScreen(null);
                setEnabled(false);
            }
        });
    }

    private static float wrapDegrees(float angle) {
        angle %= 360.0f;
        if (angle >= 180.0f) angle -= 360.0f;
        if (angle < -180.0f) angle += 360.0f;
        return angle;
    }

    private static Field findField(Class<?> type, String name) {
        for (Class<?> current = type; current != null; current = current.getSuperclass()) {
            try { return current.getDeclaredField(name); }
            catch (NoSuchFieldException ignored) {}
        }
        return null;
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
