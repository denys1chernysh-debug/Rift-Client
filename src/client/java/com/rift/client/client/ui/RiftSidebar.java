package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public final class RiftSidebar {
    private RiftSidebar() {}

    public static final String[] CATEGORIES = {
        "Combat", "Player", "Render", "Misc", "Donut", "Client", "Favorites"
    };
    private static final String[] ICONS = {"⚔", "♙", "◉", "▦", "◈", "⚙", "☆"};

    public static void render(GuiGraphicsExtractor g, Font font, int x, int y, int sidebarWidth, int height, int selected, int favorites) {
        g.fill(x, y, x + sidebarWidth, y + height, RiftTheme.SIDEBAR);

        // Brand block
        g.fill(x + 16, y + 16, x + 42, y + 42, 0xFF7544D5);
        RiftTheme.text(g, font, "ϟ", x + 24, y + 21, RiftTheme.WHITE);
        RiftTheme.text(g, font, "RIFT", x + 51, y + 18, RiftTheme.WHITE);
        RiftTheme.text(g, font, "FABRIC CLIENT", x + 51, y + 32, RiftTheme.GRAY);
        g.fill(x + 14, y + 55, x + sidebarWidth - 14, y + 56, RiftTheme.BORDER);

        RiftTheme.text(g, font, "W O R K S P A C E", x + 17, y + 66, RiftTheme.MUTED);
        for (int i = 0; i < CATEGORIES.length; i++) {
            int itemY = y + 88 + i * 34;
            boolean active = selected == i;
            if (active) {
                g.fill(x + 9, itemY - 5, x + sidebarWidth - 9, itemY + 25, 0xFF282039);
                g.fill(x + 9, itemY - 5, x + 12, itemY + 25, RiftTheme.PURPLE_GLOW);
            }
            RiftTheme.text(g, font, ICONS[i], x + 19, itemY + 3,
                active ? RiftTheme.PURPLE_GLOW : RiftTheme.GRAY);
            RiftTheme.text(g, font, CATEGORIES[i], x + 43, itemY + 3,
                active ? RiftTheme.WHITE : RiftTheme.GRAY);
            if (i == 6) {
                String count = String.valueOf(favorites);
                g.fill(x + sidebarWidth - 30, itemY + 1, x + sidebarWidth - 15, itemY + 17, 0xFF34264F);
                RiftTheme.text(g, font, count, x + sidebarWidth - 26, itemY + 5, 0xFFD0B8FF);
            }
        }

        // Connection / profile status card
        int profileY = y + height - 57;
        g.fill(x + 11, profileY, x + sidebarWidth - 11, y + height - 11, 0xFF151222);
        g.outline(x + 11, profileY, sidebarWidth - 22, 46, RiftTheme.BORDER);
        g.fill(x + 20, profileY + 18, x + 26, profileY + 24, RiftTheme.GREEN);
        RiftTheme.text(g, font, "Default", x + 34, profileY + 11, RiftTheme.WHITE);
        RiftTheme.text(g, font, "RIFT IS CONNECTED", x + 34, profileY + 25, RiftTheme.GRAY);
        RiftTheme.text(g, font, "v1.0", x + sidebarWidth - 35, profileY + 17, 0xFFB9A7D8);
    }
}