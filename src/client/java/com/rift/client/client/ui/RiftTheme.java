package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public final class RiftTheme {
    private RiftTheme() {}

    public static final int BACKGROUND = 0xFF090909;
    public static final int SIDEBAR = 0xFF0D0D0D;
    public static final int PANEL = 0xFF141414;
    public static final int PANEL_HOVER = 0xFF1B1B1B;
    public static final int PURPLE = 0xFF858585;
    public static final int PURPLE_GLOW = 0xFFB8B8B8;
    public static final int BORDER = 0xFF303030;
    public static final int WHITE = 0xFFF0F0F0;
    public static final int GRAY = 0xFF9A9A9A;
    public static final int MUTED = 0xFF686868;
    public static final int GREEN = 0xFFB0B0B0;

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
        roundedPanel(g, x, y, width, height, Math.max(5, Math.min(12, height / 4)), selected ? PANEL_HOVER : PANEL,
            selected ? PURPLE : BORDER);
    }

    public static void text(GuiGraphicsExtractor g, Font font, String message, int x, int y, int color) {
        g.text(font, message, x, y, color, false);
    }

    public static void toggle(GuiGraphicsExtractor g, int x, int y, float progress) {
        toggle(g, x, y, progress, 1f);
    }

    public static void toggle(GuiGraphicsExtractor g, int x, int y, float progress, float scale) {
        int track = progress > 0.5f ? PURPLE_GLOW : 0xFF343434;
        int w = Math.round(23 * scale), h = Math.round(13 * scale);
        roundedRect(g, x, y, w, h, Math.round(7 * scale), track);
        int knob = Math.round(9 * scale);
        int knobX = x + Math.round(2 * scale) + Math.round(10 * scale * progress);
        roundedRect(g, knobX, y + Math.round(2 * scale), knob, knob, Math.max(2, Math.round(5 * scale)), WHITE);
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