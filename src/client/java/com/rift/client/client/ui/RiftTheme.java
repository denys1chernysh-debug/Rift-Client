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

    public static void panel(GuiGraphicsExtractor g, int x, int y, int width, int height, boolean selected) {
        g.fill(x, y, x + width, y + height, selected ? PANEL_HOVER : PANEL);
        g.outline(x, y, width, height, selected ? PURPLE : BORDER);
    }

    public static void text(GuiGraphicsExtractor g, Font font, String message, int x, int y, int color) {
        g.text(font, message, x, y, color, false);
    }

    public static void toggle(GuiGraphicsExtractor g, int x, int y, float progress) {
        int track = progress > 0.5f ? PURPLE_GLOW : 0xFF343044;
        g.fill(x, y, x + 38, y + 20, track);
        int knobX = x + 3 + Math.round(17 * progress);
        g.fill(knobX, y + 3, knobX + 14, y + 17, WHITE);
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