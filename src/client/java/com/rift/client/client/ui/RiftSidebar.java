package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public final class RiftSidebar {
    private RiftSidebar() {}

    public static final String[] CATEGORIES = {
        "Combat", "Player", "Render", "Misc", "Donut", "Client", "Favorites"
    };
    private static final String[] ICONS = {"⚔", "♙", "◉", "▦", "◈", "⚙", "☆"};

    private static int p(float value, float scale) {
        return Math.round(value * scale);
    }

    public static void render(GuiGraphicsExtractor g, Font font, int x, int y, int sidebarWidth, int height, int selected, int favorites) {
        float s = sidebarWidth / 152f;
        g.fill(x, y, x + sidebarWidth, y + height, RiftTheme.SIDEBAR);

        // Brand header
        RiftTheme.roundedRect(g, x + p(16, s), y + p(16, s), p(26, s), p(26, s), p(8, s), 0xFF7544D5);
        RiftTheme.text(g, font, "ϟ", x + p(24, s), y + p(21, s), RiftTheme.WHITE);
        RiftTheme.text(g, font, "RIFT", x + p(51, s), y + p(18, s), RiftTheme.WHITE);
        RiftTheme.text(g, font, "FABRIC CLIENT", x + p(51, s), y + p(32, s), RiftTheme.GRAY);
        g.fill(x + p(14, s), y + p(55, s), x + sidebarWidth - p(14, s), y + p(56, s), RiftTheme.BORDER);

        RiftTheme.text(g, font, "W O R K S P A C E", x + p(17, s), y + p(66, s), RiftTheme.MUTED);
        for (int i = 0; i < CATEGORIES.length; i++) {
            int itemY = y + p(88 + i * 27, s);
            boolean active = selected == i;
            if (active) {
                RiftTheme.roundedRect(g, x + p(9, s), itemY - p(2, s),
                    sidebarWidth - p(18, s), p(25, s), p(8, s), 0xFF282039);
                RiftTheme.roundedRect(g, x + p(9, s), itemY - p(2, s),
                    p(3, s), p(25, s), p(2, s), RiftTheme.PURPLE_GLOW);
            }
            RiftTheme.text(g, font, ICONS[i], x + p(19, s), itemY + p(3, s),
                active ? RiftTheme.PURPLE_GLOW : RiftTheme.GRAY);
            RiftTheme.text(g, font, CATEGORIES[i], x + p(43, s), itemY + p(3, s),
                active ? RiftTheme.WHITE : RiftTheme.GRAY);
            if (i == 6) {
                String count = String.valueOf(favorites);
                g.fill(x + sidebarWidth - p(30, s), itemY + p(1, s),
                    x + sidebarWidth - p(15, s), itemY + p(17, s), 0xFF34264F);
                RiftTheme.text(g, font, count, x + sidebarWidth - p(26, s),
                    itemY + p(5, s), 0xFFD0B8FF);
            }
        }

        // Connection / profile status card
        int profileY = y + height - p(57, s);
        RiftTheme.roundedPanel(g, x + p(11, s), profileY, sidebarWidth - p(22, s),
            p(46, s), p(8, s), 0xFF151222, RiftTheme.BORDER);
        RiftTheme.roundedRect(g, x + p(20, s), profileY + p(18, s),
            p(6, s), p(6, s), p(3, s), RiftTheme.GREEN);
        RiftTheme.text(g, font, "Default", x + p(34, s), profileY + p(11, s), RiftTheme.WHITE);
        RiftTheme.text(g, font, "RIFT IS CONNECTED", x + p(34, s),
            profileY + p(25, s), RiftTheme.GRAY);
        RiftTheme.text(g, font, "v1.0", x + sidebarWidth - p(35, s),
            profileY + p(17, s), 0xFFB9A7D8);
    }
}
