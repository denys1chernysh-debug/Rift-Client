package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public final class RiftTheme {
    private RiftTheme() {}

    public static final int BACKGROUND = 0xFF0B0A12;
    public static final int SIDEBAR = 0xFF100E1B;
    public static final int PANEL = 0xFF161324;
    public static final int PANEL_HOVER = 0xFF1D1930;
    public static final int PURPLE = 0xFF8B5CF6;
    public static final int PURPLE_GLOW = 0xFFA855F7;
    public static final int BORDER = 0xFF241F38;
    public static final int WHITE = 0xFFF8FAFC;
    public static final int GRAY = 0xFF8E8AA8;
    public static final int MUTED = 0xFF68627F;
    public static final int GREEN = 0xFF4ADE80;

    /** Draw a rounded rectangle using horizontal scanlines (Minecraft GUI has no CSS-style radius primitive). */
    public static void roundedRect(GuiGraphicsExtractor g, int x, int y, int width, int height, int radius, int color) {
        if (width <= 0 || height <= 0) return;
        int r = Math.max(0, Math.min(radius, Math.min(width, height) / 2));
        if (r == 0) {
            g.fill(x, y, x + width, y + height, color);
            return;
        }
        for (int row = 0; row < height; row++) {
            double dy = row < r ? r - row - 0.5
                : row >= height - r ? row - (height - r) + 0.5 : 0.0;
            int inset = dy <= 0 ? 0
                : (int)Math.ceil(r - Math.sqrt(Math.max(0.0, r * r - dy * dy)));
            g.fill(x + inset, y + row, x + width - inset, y + row + 1, color);
        }
    }

    public static void roundedPanel(GuiGraphicsExtractor g, int x, int y, int width, int height,
                                    int radius, int fill, int border) {
        roundedRect(g, x, y, width, height, radius, border);
        roundedRect(g, x + 1, y + 1, width - 2, height - 2, Math.max(1, radius - 1), fill);
    }

    public static void panel(GuiGraphicsExtractor g, int x, int y, int width, int height, boolean selected) {
        roundedPanel(g, x, y, width, height, 7, selected ? PANEL_HOVER : PANEL,
            selected ? PURPLE : BORDER);
    }

    public static void text(GuiGraphicsExtractor g, Font font, String message, int x, int y, int color) {
        g.text(font, message, x, y, color, false);
    }

    public static void toggle(GuiGraphicsExtractor g, int x, int y, float progress) {
        int track = progress > 0.5f ? PURPLE_GLOW : 0xFF343044;
        roundedRect(g, x, y, 23, 13, 7, track);
        int knobX = x + 2 + Math.round(10 * progress);
        roundedRect(g, knobX, y + 2, 9, 9, 5, WHITE);
    }

    public static int mix(int from, int to, float amount) {
        amount = Math.max(0, Math.min(1, amount));
        int a = (int)(((from >>> 24) & 255) * (1 - amount) + ((to >>> 24) & 255) * amount);
        int r = (int)(((from >>> 16) & 255) * (1 - amount) + ((to >>> 16) & 255) * amount);
        int gr = (int)(((from >>> 8) & 255) * (1 - amount) + ((to >>> 8) & 255) * amount);
        int b = (int)((from & 255) * (1 - amount) + (to & 255) * amount);
        return (a << 24) | (r << 16) | (gr << 8) | b;
    }
}