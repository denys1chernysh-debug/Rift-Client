package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public class RiftModuleCard {
    public static void render(
        GuiGraphicsExtractor g,
        Font font,
        int x,
        int y,
        String name,
        String description,
        boolean enabled,
        boolean implemented,
        boolean hovered
    ) {
        RiftTheme.panel(g, x, y, 235, 64, hovered || enabled);
        RiftTheme.text(g, font, name, x + 13, y + 13, RiftTheme.WHITE);
        RiftTheme.text(g, font, description, x + 13, y + 32, RiftTheme.GRAY);
        if (!implemented) {
            RiftTheme.text(g, font, "IN DEVELOPMENT", x + 13, y + 49, 0xFFB18BCF);
            g.fill(x + 184, y + 22, x + 222, y + 42, 0xFF30263A);
            g.outline(x + 184, y + 22, 38, 20, RiftTheme.BORDER);
            RiftTheme.text(g, font, "WIP", x + 194, y + 28, RiftTheme.GRAY);
        } else {
            RiftTheme.toggle(g, x + 183, y + 22, enabled);
        }
    }
}
