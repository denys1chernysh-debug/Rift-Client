
package com.rift.client.client;

import com.rift.client.client.ui.RiftTheme;
import com.rift.client.client.ui.RiftSidebar;
import com.rift.client.client.ui.RiftModuleCard;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class RiftScreen extends Screen {

    private int page = 0;
    private int settingsModule = -1;

    private final String[][] modules = {
        {"Combat HUD", "Cooldown Display", "Totem Counter"},
        {"Sprint", "Movement HUD", "Inventory HUD"},
        {"Item Culler", "Particle Reducer", "Low Animations", "FPS Display"},
        {"Keystrokes", "Coordinates", "Ping Display", "Clock"},
        {"Donut HUD", "Chunk Information", "Server Information", "Statistics"},
        {"Custom Theme", "Notifications", "Profiles", "HUD Editor"},
        {"FPS Display", "Coordinates", "Item Culler"}
    };

    private final String[][] descriptions = {
        {"Shows combat information", "Tracks ability cooldowns", "Displays totem count"},
        {"Movement controls", "Shows movement stats", "Displays inventory"},
        {"Reduces dropped item rendering", "Reduces visual particles", "Simplifies animations", "Shows frames per second"},
        {"Displays keyboard presses", "Shows your coordinates", "Shows server ping", "Displays the clock"},
        {"Donut server information HUD", "Displays chunk information", "Shows server details", "Displays statistics"},
        {"Change the client theme", "Client alerts", "Manage profiles", "Edit HUD layout"},
        {"Shows frames per second", "Shows your coordinates", "Reduces item rendering"}
    };

    private final boolean[][] enabled = new boolean[7][10];

    private final String[] categories = {
        "Combat", "Player", "Render", "Misc",
        "Donut", "Client", "Favorites"
    };

    public RiftScreen() {
        super(Component.literal("RIFT"));
    }

    @Override
    public void extractRenderState(
        GuiGraphicsExtractor g,
        int mouseX,
        int mouseY,
        float delta
    ) {
        super.extractRenderState(g, mouseX, mouseY, delta);

        int x = (width - 720) / 2;
        int y = (height - 420) / 2;

        g.fill(0, 0, width, height, 0xDD05030A);

        g.fill(x, y, x + 720, y + 420, RiftTheme.BACKGROUND);
        g.outline(x, y, 720, 420, RiftTheme.PURPLE);

        RiftSidebar.render(g, minecraft.font, x, y, page);

        RiftTheme.text(g, minecraft.font, categories[page],
            x + 175, y + 22, RiftTheme.WHITE);

        RiftTheme.text(g, minecraft.font, "Manage your RIFT modules",
            x + 175, y + 42, RiftTheme.GRAY);

        RiftTheme.panel(g, x + 175, y + 65, 515, 27, false);

        RiftTheme.text(g, minecraft.font, "Search modules...",
            x + 188, y + 75, RiftTheme.GRAY);

        for (int i = 0; i < modules[page].length; i++) {

            int col = i % 2;
            int row = i / 2;

            int cardX = x + 175 + col * 260;
            int cardY = y + 110 + row * 65;

            boolean hovered =
                mouseX >= cardX && mouseX <= cardX + 235 &&
                mouseY >= cardY && mouseY <= cardY + 65;

            RiftModuleCard.render(
                g,
                minecraft.font,
                cardX,
                cardY,
                modules[page][i],
                descriptions[page][i],
                enabled[page][i],
                hovered
            );
        }

        if (settingsModule >= 0) {

            RiftTheme.panel(g, x + 175, y + 310, 515, 75, true);

            RiftTheme.text(g, minecraft.font, "MODULE SETTINGS",
                x + 190, y + 325, RiftTheme.PURPLE);

            RiftTheme.text(g, minecraft.font,
                modules[page][settingsModule],
                x + 190, y + 345, RiftTheme.WHITE);

            RiftTheme.text(g, minecraft.font,
                "Configuration options coming next",
                x + 190, y + 363, RiftTheme.GRAY);
        }

        RiftTheme.text(g, minecraft.font, "RIFT CLIENT  |  1.0.0",
            x + 175, y + 400, RiftTheme.GRAY);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {

        double mouseX = event.x();
        double mouseY = event.y();

        if (event.button() != 0) {
            return super.mouseClicked(event, doubleClick);
        }

        int x = (width - 720) / 2;
        int y = (height - 420) / 2;

        for (int i = 0; i < categories.length; i++) {

            int itemY = y + 70 + i * 39;

            if (mouseX >= x + 9 && mouseX <= x + 140 &&
                mouseY >= itemY - 7 && mouseY <= itemY + 19) {

                page = i;
                settingsModule = -1;
                return true;
            }
        }

        for (int i = 0; i < modules[page].length; i++) {

            int col = i % 2;
            int row = i / 2;

            int cardX = x + 175 + col * 260;
            int cardY = y + 110 + row * 65;

            if (mouseX >= cardX && mouseX <= cardX + 235 &&
                mouseY >= cardY && mouseY <= cardY + 65) {

                if (mouseX >= cardX + 180) {
                    settingsModule = i;
                } else {
                    enabled[page][i] = !enabled[page][i];
                    settingsModule = -1;
                }

                return true;
            }
        }

        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
