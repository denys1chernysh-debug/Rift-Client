package com.rift.client.client.ui;

import com.rift.client.client.modules.Module;
import com.rift.client.client.modules.ModuleManager;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;
import net.minecraft.client.player.LocalPlayer;

public final class RiftHud {
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    private RiftHud() {}

    public static void extract(GuiGraphicsExtractor graphics, net.minecraft.client.DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.gui.screen() != null) return;

        Font font = mc.font;
        int x = 10;
        int y = 10;
        int line = 13;

        for (Module module : ModuleManager.getModules()) {
            if (!module.isEnabled()) continue;
            String value = switch (module.getName()) {
                case "Coordinates" -> String.format("XYZ  %.1f / %.1f / %.1f",
                    player.getX(), player.getY(), player.getZ());
                case "FPS Display" -> "FPS  " + mc.getFps();
                case "Chunk Information" -> {
                    var chunk = player.chunkPosition();
                    yield "CHUNK  " + chunk.x + " / " + chunk.z;
                }
                case "Totem Counter" -> "TOTEMS  " + player.getInventory().countItem(net.minecraft.world.item.Items.TOTEM_OF_UNDYING);
                case "Clock" -> "TIME  " + LocalTime.now().format(CLOCK);
                case "Movement HUD" -> String.format("SPEED  %.2f", Math.sqrt(
                    player.getDeltaMovement().x * player.getDeltaMovement().x
                    + player.getDeltaMovement().z * player.getDeltaMovement().z));
                case "Combat HUD" -> "RIFT  COMBAT HUD";
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
