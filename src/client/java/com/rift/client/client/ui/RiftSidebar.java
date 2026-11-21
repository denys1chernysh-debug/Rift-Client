package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public final class RiftSidebar {
    private RiftSidebar() {}

    public static final String[] CATEGORIES = {
        "Combat", "Crystal", "Mace", "Donut", "Player", "Visuals", "Client", "Favorites"
    };
    private static final String[] ICONS = {"◎", "◇", "✦", "◈", "↗", "◉", "⚙", "☆"};

    public static void render(GuiGraphicsExtractor g, Font font, int x, int y, int sidebarWidth, int height, int selected, int favorites) {
        g.fill(x, y, x + sidebarWidth, y + height, RiftTheme.SIDEBAR);
        RiftTheme.text(g, font, "RIFT", x + 22, y + 20, RiftTheme.WHITE);
        RiftTheme.text(g, font, "CLIENT  /  26.2", x + 22, y + 35, RiftTheme.GRAY);
        g.fill(x + 16, y + 55, x + sidebarWidth - 16, y + 56, RiftTheme.BORDER);

        RiftTheme.text(g, font, "WORKSPACE", x + 20, y + 64, RiftTheme.MUTED);
        for (int i = 0; i < CATEGORIES.length; i++) {
            int itemY = y + 88 + i * 34;
            boolean active = selected == i;
            if (active) {
                g.fill(x + 10, itemY - 5, x + sidebarWidth - 10, itemY + 25, 0xFF292039);
                g.fill(x + 10, itemY - 5, x + 12, itemY + 25, RiftTheme.PURPLE);
            }
            RiftTheme.text(g, font, ICONS[i], x + 21, itemY + 3,
                active ? RiftTheme.PURPLE : RiftTheme.GRAY);
            RiftTheme.text(g, font, CATEGORIES[i], x + 43, itemY + 3,
                active ? RiftTheme.WHITE : RiftTheme.GRAY);
            if (i == 7 && favorites > 0) {
                RiftTheme.text(g, font, String.valueOf(favorites), x + sidebarWidth - 28, itemY + 3, RiftTheme.PURPLE);
            }
        }

        int profileY = y + height - 55;
        g.fill(x + 12, profileY, x + sidebarWidth - 12, y + height - 12, 0xFF191521);
        g.outline(x + 12, profileY, sidebarWidth - 24, 43, RiftTheme.BORDER);
        g.fill(x + 21, profileY + 10, x + 42, profileY + 31, 0xFF6F42A5);
        RiftTheme.text(g, font, "R", x + 28, profileY + 16, RiftTheme.WHITE);
        RiftTheme.text(g, font, "RIFT USER", x + 49, profileY + 9, RiftTheme.WHITE);
        RiftTheme.text(g, font, "LOCAL PROFILE", x + 49, profileY + 23, RiftTheme.GRAY);
    }
}