package com.rift.client.client.ui;

import com.rift.client.client.modules.Module;
import com.rift.client.client.modules.ModuleManager;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;

public final class RiftHud {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");
    private RiftHud() {}

    public static void extract(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.screen() != null) return;

        Font font = mc.font;
        int x = 10, y = 10;
        int line = 13;

        for (Module module : ModuleManager.getModules()) {
            if (!module.isEnabled()) continue;
            String value = switch (module.getName()) {
                case "Coordinates" -> String.format("XYZ  %.1f / %.1f / %.1f",
                    player.getX(), player.getY(), player.getZ());
                case "FPS Display" -> "FPS  " + mc.getFps();
                case "Chunk Information" -> "CHUNK  " + (player.getBlockX() >> 4) + " / " + (player.getBlockZ() >> 4);
                case "Totem Counter" -> "TOTEMS  " + player.getInventory().countItem(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
                case "Clock" -> "TIME  " + LocalTime.now().format(CLOCK);
                case "Movement HUD" -> "SPEED  " + String.format("%.2f", Math.sqrt(
                    player.getDeltaMovement().x * player.getDeltaMovement().x
                    + player.getDeltaMovement().z * player.getDeltaMovement().z));
                case "Combat HUD" -> "RIFT  COMBAT HUD";
                case "Totem Status" -> "TOTEMS  " + player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
                case "Crystal Status" -> "CRYSTALS  " + player.getInventory().countItem(Items.END_CRYSTAL);
                case "Anchor Status" -> "ANCHORS  " + player.getInventory().countItem(Items.RESPAWN_ANCHOR);
                case "Mace Status" -> "MACE  " + player.getInventory().countItem(Items.MACE)
                    + "  |  WIND CHARGES  " + player.getInventory().countItem(Items.WIND_CHARGE);
                case "Cart Status" -> "MINECARTS  " + player.getInventory().countItem(Items.MINECART);
                case "Statistics" -> "LEVEL  " + player.experienceLevel + "  |  HEALTH  "
                    + String.format("%.1f", player.getHealth());
                case "Donut HUD" -> "DONUT  XYZ " + player.getBlockX() + " / " + player.getBlockY()
                    + " / " + player.getBlockZ() + "  |  TOTEMS "
                    + player.getInventory().countItem(Items.TOTEM_OF_UNDYING);
                case "Live Player Finder" -> "VISIBLE PLAYERS  " + mc.level.players().size();
                case "Inventory Display" -> "INVENTORY  TOTEMS "
                    + player.getInventory().countItem(Items.TOTEM_OF_UNDYING)
                    + "  |  PEARLS " + player.getInventory().countItem(Items.ENDER_PEARL)
                    + "  |  XP " + player.getInventory().countItem(Items.EXPERIENCE_BOTTLE);
                case "Cooldown HUD" -> "ATTACK COOLDOWN  "
                    + Math.round(player.getAttackStrengthScale(0.0F) * 100) + "%";
                case "Armor Status" -> {
                    int equipped = 0;
                    for (ItemStack stack : player.getArmorSlots()) if (!stack.isEmpty()) equipped++;
                    yield "ARMOR  " + equipped + "/4 PIECES EQUIPPED";
                }
                default -> null;
            };
            if (value == null) continue;
            graphics.fill(x - 4, y - 3, x + font.width(value) + 5, y + 10, 0xB90B0812);
            graphics.fill(x - 4, y - 3, x - 2, y + 10, RiftTheme.PURPLE);
            graphics.text(font, value, x, y, RiftTheme.WHITE, false);
            y += line + 3;
        }
    }
}
