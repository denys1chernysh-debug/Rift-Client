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
        g.fill(x, y, x + width, y + height, hovered ? 0xFF211333 : 0xFF140C23);
        g.outline(x, y, width, height, hovered ? 0xFF7540A7 : 0xFF2B1B3D);
        if (enabledProgress > 0.02f) {
            g.fill(x, y + 9, x + 2, y + height - 9,
                RiftTheme.mix(0x00170D2A, 0xFFB45AFF, enabledProgress));
        }
        RiftTheme.text(g, font, name, x + 10, y + 8, RiftTheme.WHITE);
        String desc = description.length() > 42 ? description.substring(0, 39) + "..." : description;
        RiftTheme.text(g, font, desc, x + 10, y + 23, RiftTheme.GRAY);

        RiftTheme.text(g, font, pinned ? "★" : "☆", x + width - 80, y + 5,
            pinned ? RiftTheme.PURPLE : RiftTheme.MUTED);
        RiftTheme.text(g, font, "⚙", x + width - 63, y + 17, RiftTheme.GRAY);

        if (implemented) {
            RiftTheme.toggle(g, x + width - 49, y + 13, enabledProgress);
        } else {
            g.fill(x + width - 49, y + 13, x + width - 8, y + 33, 0xFF241A30);
            g.outline(x + width - 49, y + 13, 41, 20, 0xFF3A2B49);
            RiftTheme.text(g, font, "WIP", x + width - 39, y + 19, RiftTheme.MUTED);
        }
    }
}