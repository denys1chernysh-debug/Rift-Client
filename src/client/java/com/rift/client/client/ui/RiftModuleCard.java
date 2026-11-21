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
        int background = hovered ? 0xFF1D1930 : RiftTheme.PANEL;
        int border = hovered ? 0xFF493765 : RiftTheme.BORDER;
        RiftTheme.roundedPanel(g, x, y, width, height, 8, background, border);
        if (enabledProgress > 0.02f) {
            g.fill(x, y + 7, x + 2, y + height - 7, RiftTheme.PURPLE_GLOW);
        }

        RiftTheme.text(g, font, name, x + 12, y + 8, RiftTheme.WHITE);
        String desc = description.length() > 39 ? description.substring(0, 36) + "..." : description;
        RiftTheme.text(g, font, desc, x + 12, y + 24, RiftTheme.GRAY);

        RiftTheme.text(g, font, pinned ? "★" : "☆", x + width - 83, y + 8,
            pinned ? 0xFFC4A7FF : RiftTheme.MUTED);
        g.fill(x + width - 65, y + 6, x + width - 46, y + 25, 0xFF211B30);
        g.outline(x + width - 65, y + 6, 19, 19, RiftTheme.BORDER);
        RiftTheme.text(g, font, "⚙", x + width - 60, y + 9, RiftTheme.GRAY);

        if (implemented) {
            RiftTheme.toggle(g, x + width - 43, y + 13, enabledProgress);
        } else {
            RiftTheme.roundedPanel(g, x + width - 43, y + 13, 38, 20, 10, 0xFF24202B, 0xFF403747);
            RiftTheme.text(g, font, "—", x + width - 28, y + 19, RiftTheme.MUTED);
        }
    }
}