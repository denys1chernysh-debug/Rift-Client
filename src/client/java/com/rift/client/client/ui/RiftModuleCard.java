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
        int background = hovered ? 0xFF211B2A : 0xFF17131E;
        int border = hovered ? 0xFF60427D : 0xFF302839;
        g.fill(x, y, x + width, y + height, background);
        g.outline(x, y, width, height, border);
        if (enabledProgress > 0.02f) {
            g.fill(x, y + 7, x + 2, y + height - 7, RiftTheme.PURPLE);
        }

        RiftTheme.text(g, font, name, x + 12, y + 8, RiftTheme.WHITE);
        String desc = description.length() > 39 ? description.substring(0, 36) + "..." : description;
        RiftTheme.text(g, font, desc, x + 12, y + 24, RiftTheme.GRAY);

        RiftTheme.text(g, font, pinned ? "★" : "☆", x + width - 83, y + 8,
            pinned ? RiftTheme.PURPLE : RiftTheme.MUTED);
        RiftTheme.text(g, font, "⚙", x + width - 64, y + 8, RiftTheme.GRAY);

        if (implemented) {
            RiftTheme.toggle(g, x + width - 49, y + 13, enabledProgress);
        } else {
            g.fill(x + width - 49, y + 13, x + width - 8, y + 33, 0xFF24202B);
            g.outline(x + width - 49, y + 13, 41, 20, 0xFF403747);
            RiftTheme.text(g, font, "—", x + width - 33, y + 19, RiftTheme.MUTED);
        }
    }
}