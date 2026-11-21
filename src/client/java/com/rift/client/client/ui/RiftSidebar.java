
package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public class RiftSidebar {

    public static final String[] CATEGORIES = {
        "Combat",
        "Player",
        "Render",
        "Misc",
        "Donut",
        "Client",
        "Favorites"
    };

    public static void render(
        GuiGraphicsExtractor g,
        Font font,
        int x,
        int y,
        int selected
    ) {
        g.fill(x, y, x + 155, y + 420, RiftTheme.SIDEBAR);

        RiftTheme.text(g, font, "RIFT",
            x + 22, y + 22, RiftTheme.PURPLE);

        RiftTheme.text(g, font, "CLIENT",
            x + 70, y + 24, RiftTheme.GRAY);

        g.fill(x + 15, y + 49, x + 140, y + 50,
            RiftTheme.BORDER);

        for (int i = 0; i < CATEGORIES.length; i++) {
            int itemY = y + 72 + i * 43;

            if (selected == i) {
                g.fill(
                    x + 10, itemY - 8,
                    x + 145, itemY + 20,
                    RiftTheme.PANEL_HOVER
                );

                g.fill(
                    x + 10, itemY - 8,
                    x + 13, itemY + 20,
                    RiftTheme.PURPLE
                );
            }

            RiftTheme.text(
                g,
                font,
                CATEGORIES[i],
                x + 27,
                itemY,
                selected == i
                    ? RiftTheme.WHITE
                    : RiftTheme.GRAY
            );
        }

        // Profile
        RiftTheme.panel(g, x + 12, y + 365, 131, 42, false);

        g.fill(
            x + 21, y + 374,
            x + 45, y + 398,
            RiftTheme.PURPLE_GLOW
        );

        RiftTheme.text(g, font, "R",
            x + 29, y + 382, RiftTheme.WHITE);

        RiftTheme.text(g, font, "RIFT User",
            x + 53, y + 377, RiftTheme.WHITE);

        RiftTheme.text(g, font, "Premium Client",
            x + 53, y + 390, RiftTheme.GRAY);
    }
}
