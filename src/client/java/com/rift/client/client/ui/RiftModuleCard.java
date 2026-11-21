
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
        boolean hovered
    ) {
        // Card background
        RiftTheme.panel(g, x, y, 235, 65, hovered || enabled);

        // Module name
        RiftTheme.text(
            g, font, name,
            x + 14, y + 14,
            RiftTheme.WHITE
        );

        // Description
        RiftTheme.text(
            g, font, description,
            x + 14, y + 34,
            RiftTheme.GRAY
        );

        // Toggle
        RiftTheme.toggle(
            g,
            x + 183,
            y + 22,
            enabled
        );
    }
}
