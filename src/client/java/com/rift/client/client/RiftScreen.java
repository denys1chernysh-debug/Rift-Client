package com.rift.client.client;

import com.rift.client.client.modules.Module;
import com.rift.client.client.modules.ModuleManager;
import com.rift.client.client.ui.RiftModuleCard;
import com.rift.client.client.ui.RiftSidebar;
import com.rift.client.client.ui.RiftTheme;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class RiftScreen extends Screen {
    private static final int W = 720, H = 420;
    private int page;
    private int scroll;
    private String search = "";
    private boolean searchFocused;
    private Module settingsModule;

    public RiftScreen() {
        super(Component.literal("RIFT"));
    }

    private List<Module> visibleModules() {
        List<Module> result = new ArrayList<>();
        String term = search.toLowerCase(Locale.ROOT).trim();
        for (Module module : ModuleManager.getModules()) {
            boolean inCategory = page == 6
                ? module.isPinned()
                : module.getCategory().equalsIgnoreCase(RiftSidebar.CATEGORIES[page]);
            boolean matches = term.isEmpty()
                || module.getName().toLowerCase(Locale.ROOT).contains(term)
                || module.getDescription().toLowerCase(Locale.ROOT).contains(term);
            if ((inCategory || !term.isEmpty()) && matches) result.add(module);
        }
        return result;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int x = (width - W) / 2;
        int y = (height - H) / 2;

        g.fill(0, 0, width, height, 0xDD05030A);
        g.fill(x, y, x + W, y + H, RiftTheme.BACKGROUND);
        g.outline(x, y, W, H, RiftTheme.PURPLE);
        g.fill(x + 1, y + 1, x + 155, y + H - 1, RiftTheme.SIDEBAR);
        RiftSidebar.render(g, minecraft.font, x, y, page);

        List<Module> modules = visibleModules();
        String heading = search.isEmpty() ? RiftSidebar.CATEGORIES[page] : "Search";
        RiftTheme.text(g, minecraft.font, heading, x + 175, y + 22, RiftTheme.WHITE);
        RiftTheme.text(g, minecraft.font,
            search.isEmpty() ? "Configure your RIFT experience" : modules.size() + " matching modules",
            x + 175, y + 42, RiftTheme.GRAY);

        RiftTheme.panel(g, x + 175, y + 61, 515, 30, searchFocused);
        RiftTheme.text(g, minecraft.font, search.isEmpty() ? "⌕  Search modules..." : "⌕  " + search,
            x + 188, y + 72, search.isEmpty() ? RiftTheme.GRAY : RiftTheme.WHITE);
        RiftTheme.text(g, minecraft.font, "×", x + 674, y + 70, RiftTheme.GRAY);

        int maxScroll = Math.max(0, modules.size() - 6);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        List<Module> shown = modules.subList(scroll, Math.min(modules.size(), scroll + 6));

        if (shown.isEmpty()) {
            RiftTheme.panel(g, x + 175, y + 110, 515, 205, false);
            RiftTheme.text(g, minecraft.font, "No modules found", x + 195, y + 145, RiftTheme.WHITE);
            RiftTheme.text(g, minecraft.font, "Try another search or choose a different category.",
                x + 195, y + 166, RiftTheme.GRAY);
        }

        for (int i = 0; i < shown.size(); i++) {
            Module module = shown.get(i);
            int cardX = x + 175 + (i % 2) * 260;
            int cardY = y + 105 + (i / 2) * 70;
            boolean hovered = mouseX >= cardX && mouseX <= cardX + 235
                && mouseY >= cardY && mouseY <= cardY + 64;
            RiftModuleCard.render(g, minecraft.font, cardX, cardY,
                module.getName(), module.getDescription(), module.isEnabled(),
                module.isImplemented(), hovered);
            if (module.isPinned()) {
                RiftTheme.text(g, minecraft.font, "★", cardX + 216, cardY + 5, RiftTheme.PURPLE);
            }
        }

        RiftTheme.text(g, minecraft.font,
            ModuleManager.getModules().size() + " MODULES  •  " + ModuleManager.getFavorites().size() + " FAVORITES",
            x + 175, y + 330, RiftTheme.GRAY);

        if (settingsModule != null) {
            RiftTheme.panel(g, x + 175, y + 346, 515, 45, true);
            RiftTheme.text(g, minecraft.font, settingsModule.getName(),
                x + 188, y + 354, RiftTheme.WHITE);
            String state = settingsModule.isImplemented()
                ? (settingsModule.isEnabled() ? "ENABLED" : "DISABLED")
                : "IN DEVELOPMENT — NOT TOGGLED YET";
            RiftTheme.text(g, minecraft.font, state, x + 188, y + 372,
                settingsModule.isImplemented() ? RiftTheme.GREEN : RiftTheme.GRAY);
            RiftTheme.text(g, minecraft.font, settingsModule.isPinned() ? "★ PINNED" : "☆ PIN",
                x + 590, y + 362, RiftTheme.PURPLE);
        } else {
            RiftTheme.text(g, minecraft.font, "RIGHT SHIFT  •  CLOSE / OPEN",
                x + 175, y + 365, RiftTheme.GRAY);
        }
        RiftTheme.text(g, minecraft.font, "RIFT CLIENT  |  FABRIC 26.2",
            x + 175, y + 400, RiftTheme.GRAY);
        if (modules.size() > 6) {
            RiftTheme.text(g, minecraft.font, (scroll + 1) + "-" + Math.min(scroll + 6, modules.size())
                + " / " + modules.size() + "  •  SCROLL TO BROWSE",
                x + 480, y + 400, RiftTheme.PURPLE);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        double mx = event.x();
        double my = event.y();
        int x = (width - W) / 2;
        int y = (height - H) / 2;

        for (int i = 0; i < RiftSidebar.CATEGORIES.length; i++) {
            int itemY = y + 72 + i * 43;
            if (mx >= x + 9 && mx <= x + 145 && my >= itemY - 8 && my <= itemY + 20) {
                page = i;
                scroll = 0;
                search = "";
                searchFocused = false;
                settingsModule = null;
                return true;
            }
        }

        if (mx >= x + 175 && mx <= x + 690 && my >= y + 61 && my <= y + 91) {
            if (mx >= x + 660) search = "";
            searchFocused = true;
            scroll = 0;
            return true;
        }

        List<Module> modules = visibleModules();
        int maxScroll = Math.max(0, modules.size() - 6);
        int shownCount = Math.min(6, Math.max(0, modules.size() - scroll));
        for (int i = 0; i < shownCount; i++) {
            int cardX = x + 175 + (i % 2) * 260;
            int cardY = y + 105 + (i / 2) * 70;
            if (mx < cardX || mx > cardX + 235 || my < cardY || my > cardY + 64) continue;
            Module module = modules.get(scroll + i);
            if (event.button() == 1) {
                module.setPinned(!module.isPinned());
                settingsModule = module;
                return true;
            }
            if (event.button() == 0) {
                if (mx >= cardX + 180) {
                    settingsModule = module;
                } else if (module.isImplemented()) {
                    module.toggle();
                    settingsModule = module;
                } else {
                    settingsModule = module;
                }
                return true;
            }
        }

        if (settingsModule != null && mx >= x + 570 && my >= y + 346 && my <= y + 391) {
            settingsModule.setPinned(!settingsModule.isPinned());
            return true;
        }
        searchFocused = false;
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchFocused) {
            String typed = event.codepointAsString();
            if (!typed.isEmpty() && typed.codePoints().allMatch(Character::isLetterOrDigit)) {
                search += typed;
                scroll = 0;
            } else if (" ".equals(typed) && !search.isEmpty()) {
                search += typed;
            }
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int x = (width - W) / 2;
        int y = (height - H) / 2;
        if (mouseX >= x + 155 && mouseY >= y + 95 && mouseY <= y + 330) {
            int max = Math.max(0, visibleModules().size() - 6);
            scroll = Math.max(0, Math.min(max, scroll + (verticalAmount < 0 ? 2 : -2)));
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
