
package com.rift.client.client.ui;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.Font;

public class RiftTheme {

    public static final int BACKGROUND = 0xF5090710;
    public static final int SIDEBAR = 0xFF100D18;
    public static final int PANEL = 0xFF191421;
    public static final int PANEL_HOVER = 0xFF251B32;

    public static final int PURPLE = 0xFFAA63F5;
    public static final int PURPLE_GLOW = 0xFF7540B5;
    public static final int BORDER = 0xFF49305F;

    public static final int WHITE = 0xFFF3EAFB;
    public static final int GRAY = 0xFF95899F;
    public static final int GREEN = 0xFF83D6A0;

    public static void panel(
        GuiGraphicsExtractor g,
        int x, int y,
        int width, int height,
        boolean selected
    ) {
        g.fill(
            x, y,
            x + width, y + height,
            selected ? PANEL_HOVER : PANEL
        );

        g.outline(
            x, y,
            width, height,
            selected ? PURPLE : BORDER
        );
    }

    public static void text(
        GuiGraphicsExtractor g,
        Font font,
        String message,
        int x, int y,
        int color
    ) {
        g.text(font, message, x, y, color, false);
    }

    public static void toggle(
        GuiGraphicsExtractor g,
        int x, int y,
        boolean enabled
    ) {
        g.fill(
            x, y,
            x + 38, y + 19,
            enabled ? PURPLE_GLOW : 0xFF393342
        );

        int knob = enabled ? x + 21 : x + 3;

        g.fill(
            knob, y + 3,
            knob + 13, y + 16,
            WHITE
        );
    }
}

