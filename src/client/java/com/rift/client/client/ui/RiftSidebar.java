package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public final class RiftSidebar {
    private RiftSidebar() {}

    public static final String[] CATEGORIES = {
        "Combat", "Player", "Render", "Misc", "Donut", "Client", "Favorites"
    };
    private static final String[] ICONS = {"◎", "♙", "◉", "▦", "◌", "➤", "☆"};

    public static void render(GuiGraphicsExtractor g, Font font, int x, int y, int sidebarWidth, int height, int selected, int favorites) {
        g.fill(x, y, x + sidebarWidth, y + height, RiftTheme.SIDEBAR);
        RiftTheme.text(g, font, "RIFT", x + 22, y + 20, RiftTheme.WHITE);
        RiftTheme.text(g, font, "F A B R I C   C L I E N T", x + 22, y + 35, RiftTheme.GRAY);
        g.fill(x + 16, y + 55, x + sidebarWidth - 16, y + 56, 0xFF332047);

        for (int i = 0; i < CATEGORIES.length; i++) {
            int itemY = y + 72 + i * 39;
            boolean active = selected == i;
            if (active) {
                g.fill(x + 11, itemY - 7, x + sidebarWidth - 12, itemY + 25, 0xFF58209B);
                g.outline(x + 11, itemY - 7, sidebarWidth - 23, 32, 0xFF8740D0);
            }
            RiftTheme.text(g, font, ICONS[i], x + 22, itemY + 2,
                active ? RiftTheme.WHITE : RiftTheme.PURPLE);
            RiftTheme.text(g, font, CATEGORIES[i], x + 44, itemY + 2,
                active ? RiftTheme.WHITE : RiftTheme.GRAY);
            if (i == 6 && favorites > 0) {
                RiftTheme.text(g, font, String.valueOf(favorites), x + sidebarWidth - 31, itemY + 2, RiftTheme.PURPLE);
            }
        }

        int profileY = y + height - 55;
        g.fill(x + 12, profileY, x + sidebarWidth - 12, y + height - 12, 0xFF1D122D);
        g.outline(x + 12, profileY, sidebarWidth - 24, 43, 0xFF39234F);
        g.fill(x + 21, profileY + 10, x + 42, profileY + 31, 0xFF8B35DE);
        RiftTheme.text(g, font, "R", x + 28, profileY + 16, RiftTheme.WHITE);
        RiftTheme.text(g, font, "Default", x + 49, profileY + 9, RiftTheme.WHITE);
        RiftTheme.text(g, font, "● RIFT connected", x + 49, profileY + 23, RiftTheme.GREEN);
    }
}