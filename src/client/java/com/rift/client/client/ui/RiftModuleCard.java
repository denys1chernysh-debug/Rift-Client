package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public final class RiftModuleCard {
    private RiftModuleCard() {}

    public static void render(
        GuiGraphicsExtractor g, Font font, int x, int y, int width, int height,
        String name, String description, float enabledProgress,
        boolean implemented, boolean pinned, boolean hovered
    ) {
        float s = width / 282f;
        int background = hovered ? 0xFF1D1930 : RiftTheme.PANEL;
        int border = hovered ? 0xFF3A3055 : RiftTheme.BORDER;
        RiftTheme.roundedPanel(g, x, y, width, height, Math.round(8 * s), background, border);
        if (enabledProgress > 0.02f) {
            g.fill(x, y + Math.round(7 * s), x + Math.max(2, Math.round(2 * s)),
                y + height - Math.round(7 * s), RiftTheme.PURPLE_GLOW);
        }

        int left = x + Math.round(15 * s);
        RiftTheme.text(g, font, name, left, y + Math.round(13 * s), RiftTheme.WHITE);
        String desc = description.length() > 54 ? description.substring(0, 51) + "..." : description;
        RiftTheme.text(g, font, desc, left, y + Math.round(34 * s), RiftTheme.GRAY);
        if (pinned) RiftTheme.text(g, font, "★", x + Math.round(3 * s), y + Math.round(13 * s), 0xFFC4A7FF);

        int settingsX = x + width - Math.round(52 * s);
        int settingsY = y + Math.round(15 * s);
        int settingsW = Math.round(18 * s);
        int settingsH = Math.round(19 * s);
        RiftTheme.roundedPanel(g, settingsX, settingsY, settingsW, settingsH,
            Math.round(6 * s), 0xFF1B1729, RiftTheme.BORDER);
        RiftTheme.text(g, font, "⚙", settingsX + Math.round(4 * s),
            settingsY + Math.round(5 * s), RiftTheme.GRAY);

        int toggleX = x + width - Math.round(30 * s);
        int toggleY = y + Math.round(18 * s);
        if (implemented) {
            RiftTheme.toggle(g, toggleX, toggleY, enabledProgress, s);
        } else {
            RiftTheme.roundedPanel(g, toggleX, toggleY, Math.round(23 * s), Math.round(13 * s),
                Math.round(7 * s), 0xFF24202B, 0xFF403747);
        }
    }
}
