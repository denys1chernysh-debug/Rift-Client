package com.rift.client.client;

import com.rift.client.client.modules.Module;
import com.rift.client.client.modules.ModuleManager;
import com.rift.client.client.ui.RiftModuleCard;
import com.rift.client.client.ui.RiftSidebar;
import com.rift.client.client.ui.RiftTheme;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class RiftScreen extends Screen {
    private static final int BASE_W = 760;
    private static final int BASE_H = 440;
    private static final int SIDEBAR_W = 156;

    private int page;
    private int scroll;
    private String search = "";
    private boolean searchFocused;
    private boolean profileMenu;
    private boolean settingsOpen;
    private Module settingsModule;
    private String profile = "Default";
    private final Map<String, Float> toggleAnimations = new HashMap<>();
    private long openedAt = System.currentTimeMillis();

    public RiftScreen() {
        super(Component.literal("RIFT"));
        loadProfile();
    }

    private List<Module> visibleModules() {
        List<Module> result = new ArrayList<>();
        String term = search.toLowerCase(Locale.ROOT).trim();
        for (Module module : ModuleManager.getModules()) {
            boolean inCategory = page == 7
                ? module.isPinned()
                : module.getCategory().equalsIgnoreCase(RiftSidebar.CATEGORIES[page]);
            boolean matches = term.isEmpty()
                || module.getName().toLowerCase(Locale.ROOT).contains(term)
                || module.getDescription().toLowerCase(Locale.ROOT).contains(term);
            if ((inCategory || !term.isEmpty()) && matches) result.add(module);
        }
        return result;
    }

    private String moduleKey(Module module) {
        return module.getCategory() + "." + module.getName();
    }

    private void loadProfile() {
        Properties properties = new Properties();
        Path path = configPath();
        try {
            if (Files.exists(path)) {
                try (var input = Files.newInputStream(path)) { properties.load(input); }
            }
            profile = properties.getProperty("profile", "Default");
            for (Module module : ModuleManager.getModules()) {
                String key = moduleKey(module);
                module.setPinned(Boolean.parseBoolean(properties.getProperty(key + ".pinned", "false")));
                if (module.isImplemented()) {
                    module.setEnabled(Boolean.parseBoolean(properties.getProperty(key + ".enabled", "false")));
                }
            }
        } catch (IOException ignored) {}
    }

    private Path configPath() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("rift-client.properties");
    }

    private void saveProfile() {
        Properties properties = new Properties();
        properties.setProperty("profile", profile);
        for (Module module : ModuleManager.getModules()) {
            String key = moduleKey(module);
            properties.setProperty(key + ".pinned", Boolean.toString(module.isPinned()));
            properties.setProperty(key + ".enabled", Boolean.toString(module.isEnabled()));
        }
        try {
            Path path = configPath();
            Files.createDirectories(path.getParent());
            try (var output = Files.newOutputStream(path)) {
                properties.store(output, "RIFT client settings");
            }
        } catch (IOException ignored) {}
    }

    private void resetSettings() {
        for (Module module : ModuleManager.getModules()) {
            module.setPinned(false);
            module.setEnabled(false);
        }
        saveProfile();
    }

    private int sx(int x, int origin, float scale) { return origin + Math.round(x * scale); }
    private int sy(int y, int origin, float scale) { return origin + Math.round(y * scale); }

    @Override
    public void extractRenderState(GuiGraphicsExtractor g, int mouseX, int mouseY, float delta) {
        super.extractRenderState(g, mouseX, mouseY, delta);
        int windowW = Math.min(BASE_W, width - 28);
        int windowH = Math.min(BASE_H, height - 28);
        int x = (width - windowW) / 2;
        int y = (height - windowH) / 2;
        float scaleX = windowW / (float) BASE_W;
        float scaleY = windowH / (float) BASE_H;
        int mx = Math.round((mouseX - x) / scaleX);
        int my = Math.round((mouseY - y) / scaleY);
        float open = Math.min(1f, (System.currentTimeMillis() - openedAt) / 190f);
        int backdrop = ((int)(0xB0 * open) << 24) | 0x00030810;
        g.fill(0, 0, width, height, backdrop);

        g.fill(x, y, x + windowW, y + windowH, RiftTheme.BACKGROUND);
        g.outline(x, y, windowW, windowH, 0xFF60328A);
        g.fill(x + 1, y + 1, x + Math.round(SIDEBAR_W * scaleX), y + windowH - 1, RiftTheme.SIDEBAR);
        RiftSidebar.render(g, minecraft.font, x, y, Math.round(SIDEBAR_W * scaleX), windowH,
            page, ModuleManager.getFavorites().size());

        int mainX = SIDEBAR_W + 18;
        int mainRight = BASE_W - 18;
        boolean searchHover = mx >= mainX && mx <= 455 && my >= 18 && my <= 48;
        RiftTheme.panel(g, sx(mainX, x, scaleX), sy(18, y, scaleY),
            Math.round(286 * scaleX), Math.round(30 * scaleY), searchFocused || searchHover);
        RiftTheme.text(g, minecraft.font, "⌕", sx(mainX + 11, x, scaleX), sy(28, y, scaleY), RiftTheme.PURPLE);
        String searchLabel = search.isEmpty() ? "Search modules..." : search;
        RiftTheme.text(g, minecraft.font, searchLabel, sx(mainX + 29, x, scaleX), sy(28, y, scaleY),
            search.isEmpty() ? RiftTheme.GRAY : RiftTheme.WHITE);
        if (!search.isEmpty()) RiftTheme.text(g, minecraft.font, "×", sx(mainX + 268, x, scaleX), sy(27, y, scaleY), RiftTheme.WHITE);

        int profileX = 574;
        RiftTheme.panel(g, sx(profileX, x, scaleX), sy(18, y, scaleY), Math.round(112 * scaleX), Math.round(30 * scaleY), profileMenu);
        g.fill(sx(profileX + 5, x, scaleX), sy(22, y, scaleY), sx(profileX + 25, x, scaleX), sy(44, y, scaleY), 0xFF8432D6);
        RiftTheme.text(g, minecraft.font, "R", sx(profileX + 12, x, scaleX), sy(28, y, scaleY), RiftTheme.WHITE);
        RiftTheme.text(g, minecraft.font, "ACTIVE PROFILE", sx(profileX + 31, x, scaleX), sy(23, y, scaleY), RiftTheme.MUTED);
        RiftTheme.text(g, minecraft.font, profile, sx(profileX + 31, x, scaleX), sy(34, y, scaleY), RiftTheme.WHITE);
        RiftTheme.text(g, minecraft.font, "⌄", sx(profileX + 98, x, scaleX), sy(29, y, scaleY), RiftTheme.GRAY);
        RiftTheme.panel(g, sx(696, x, scaleX), sy(18, y, scaleY), Math.round(30 * scaleX), Math.round(30 * scaleY), settingsOpen);
        RiftTheme.text(g, minecraft.font, "⚙", sx(704, x, scaleX), sy(27, y, scaleY), RiftTheme.WHITE);

        List<Module> modules = visibleModules();
        String heading = search.isEmpty() ? RiftSidebar.CATEGORIES[page] : "Search";
        RiftTheme.text(g, minecraft.font, "M O D U L E   C A T E G O R Y",
            sx(mainX, x, scaleX), sy(67, y, scaleY), RiftTheme.PURPLE);
        RiftTheme.text(g, minecraft.font, heading, sx(mainX, x, scaleX), sy(82, y, scaleY), RiftTheme.WHITE);
        RiftTheme.text(g, minecraft.font,
            search.isEmpty() ? categoryDescription() : modules.size() + " modules found across RIFT",
            sx(mainX, x, scaleX), sy(101, y, scaleY), RiftTheme.GRAY);
        RiftTheme.text(g, minecraft.font, String.valueOf(modules.size()),
            sx(706, x, scaleX), sy(85, y, scaleY), RiftTheme.PURPLE);
        RiftTheme.text(g, minecraft.font, "M O D U L E S",
            sx(721, x, scaleX), sy(88, y, scaleY), RiftTheme.MUTED);

        int gridX = mainX;
        int gridY = 124;
        int gap = 7;
        int cardW = 282;
        int cardH = 43;
        int rowsVisible = 5;
        int perPage = rowsVisible * 2;
        int maxScroll = Math.max(0, modules.size() - perPage);
        scroll = Math.max(0, Math.min(scroll, maxScroll));
        List<Module> shown = modules.subList(scroll, Math.min(modules.size(), scroll + perPage));

        for (int i = 0; i < shown.size(); i++) {
            Module module = shown.get(i);
            int col = i % 2;
            int row = i / 2;
            int cardX = gridX + col * (cardW + gap);
            int cardY = gridY + row * (cardH + 7);
            int actualCardX = sx(cardX, x, scaleX);
            int actualCardY = sy(cardY, y, scaleY);
            int actualCardW = Math.round(cardW * scaleX);
            int actualCardH = Math.round(cardH * scaleY);
            boolean hovered = mx >= cardX && mx <= cardX + cardW && my >= cardY && my <= cardY + cardH;
            float target = module.isEnabled() ? 1f : 0f;
            float current = toggleAnimations.getOrDefault(moduleKey(module), target);
            current += (target - current) * 0.24f;
            toggleAnimations.put(moduleKey(module), current);
            RiftModuleCard.render(g, minecraft.font, actualCardX, actualCardY, actualCardW, actualCardH,
                module.getName(), module.getDescription(), current, module.isImplemented(), module.isPinned(), hovered);
        }

        if (shown.isEmpty()) {
            RiftTheme.panel(g, sx(gridX, x, scaleX), sy(gridY, y, scaleY),
                Math.round(571 * scaleX), Math.round(160 * scaleY), false);
            RiftTheme.text(g, minecraft.font, "No modules found",
                sx(gridX + 18, x, scaleX), sy(gridY + 35, y, scaleY), RiftTheme.WHITE);
            RiftTheme.text(g, minecraft.font, "Try another search or choose a different category.",
                sx(gridX + 18, x, scaleX), sy(gridY + 55, y, scaleY), RiftTheme.GRAY);
        }

        g.fill(x + Math.round(1 * scaleX), sy(385, y, scaleY), x + windowW - 1, y + windowH - 1, 0xFF10091C);
        g.fill(x + Math.round(SIDEBAR_W * scaleX), sy(385, y, scaleY), x + windowW - 1, sy(386, y, scaleY), 0xFF2C1A40);
        RiftTheme.panel(g, sx(mainX, x, scaleX), sy(394, y, scaleY), Math.round(220 * scaleX), Math.round(34 * scaleY), false);
        RiftTheme.text(g, minecraft.font, "↶  Reset Settings",
            sx(mainX + 77, x, scaleX), sy(407, y, scaleY), RiftTheme.GRAY);
        RiftTheme.text(g, minecraft.font, "C H A N G E S   A P P L Y   I N S T A N T L Y",
            sx(406, x, scaleX), sy(408, y, scaleY), RiftTheme.MUTED);
        g.fill(sx(540, x, scaleX), sy(394, y, scaleY), sx(742, x, scaleX), sy(428, y, scaleY), 0xFF35175B);
        g.outline(sx(540, x, scaleX), sy(394, y, scaleY), Math.round(202 * scaleX), Math.round(34 * scaleY), 0xFF67339A);
        RiftTheme.text(g, minecraft.font, "↓  Save Settings",
            sx(601, x, scaleX), sy(407, y, scaleY), RiftTheme.WHITE);

        if (profileMenu) {
            int px = sx(profileX, x, scaleX);
            int py = sy(52, y, scaleY);
            RiftTheme.panel(g, px, py, Math.round(112 * scaleX), Math.round(66 * scaleY), true);
            String[] names = {"Default", "Donut SMP", "Performance"};
            for (int i = 0; i < names.length; i++) {
                RiftTheme.text(g, minecraft.font, names[i], px + 9, py + 9 + i * 18,
                    names[i].equals(profile) ? RiftTheme.PURPLE : RiftTheme.WHITE);
            }
        }

        if (settingsOpen || settingsModule != null) {
            int boxX = sx(476, x, scaleX);
            int boxY = sy(235, y, scaleY);
            RiftTheme.panel(g, boxX, boxY, Math.round(266 * scaleX), Math.round(126 * scaleY), true);
            String title = settingsModule == null ? "RIFT SETTINGS" : settingsModule.getName();
            RiftTheme.text(g, minecraft.font, title, boxX + 12, boxY + 12, RiftTheme.WHITE);
            String state = settingsModule == null ? "FABRIC CLIENT  •  v1.0" :
                settingsModule.isImplemented()
                    ? (settingsModule.isEnabled() ? "STATUS: ENABLED" : "STATUS: DISABLED")
                    : "NOT IMPLEMENTED YET";
            RiftTheme.text(g, minecraft.font, state, boxX + 12, boxY + 29,
                settingsModule != null && !settingsModule.isImplemented() ? RiftTheme.GRAY : RiftTheme.PURPLE);
            if (settingsModule != null) {
                RiftTheme.text(g, minecraft.font, settingsModule.getDescription(), boxX + 12, boxY + 48, RiftTheme.GRAY);
                RiftTheme.text(g, minecraft.font, settingsModule.isPinned() ? "★  Pinned to Favorites" : "☆  Pin to Favorites",
                    boxX + 12, boxY + 73, RiftTheme.WHITE);
                RiftTheme.text(g, minecraft.font, "CLICK TO CLOSE", boxX + 12, boxY + 105, RiftTheme.MUTED);
            } else {
                RiftTheme.text(g, minecraft.font, "Right Shift opens and closes this menu.", boxX + 12, boxY + 51, RiftTheme.GRAY);
                RiftTheme.text(g, minecraft.font, "Settings are saved in your Minecraft config folder.", boxX + 12, boxY + 69, RiftTheme.GRAY);
                RiftTheme.text(g, minecraft.font, "CLICK TO CLOSE", boxX + 12, boxY + 105, RiftTheme.MUTED);
            }
        }
    }

    private String categoryDescription() {
        return switch (page) {
            case 0 -> "Combat status, cooldowns, and equipment.";
            case 1 -> "Crystal and anchor inventory tools.";
            case 2 -> "Mace, cart, and spear information.";
            case 3 -> "Donut SMP and terrain intelligence.";
            case 4 -> "Movement and inventory utilities.";
            case 5 -> "HUDs, visuals, and render options.";
            case 6 -> "Profiles, settings, and client tools.";
            default -> "Your pinned modules and quick toggles.";
        };
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        int windowW = Math.min(BASE_W, width - 28);
        int windowH = Math.min(BASE_H, height - 28);
        int x = (width - windowW) / 2;
        int y = (height - windowH) / 2;
        float scaleX = windowW / (float) BASE_W;
        float scaleY = windowH / (float) BASE_H;
        int mx = (int) Math.round((event.x() - x) / scaleX);
        int my = (int) Math.round((event.y() - y) / scaleY);

        for (int i = 0; i < RiftSidebar.CATEGORIES.length; i++) {
            int itemY = 88 + i * 34;
            if (mx >= 8 && mx <= SIDEBAR_W - 8 && my >= itemY - 8 && my <= itemY + 27) {
                page = i; scroll = 0; search = ""; searchFocused = false;
                settingsModule = null; profileMenu = false; settingsOpen = false;
                return true;
            }
        }

        if (mx >= SIDEBAR_W + 18 && mx <= SIDEBAR_W + 304 && my >= 18 && my <= 48) {
            if (mx >= SIDEBAR_W + 270 && !search.isEmpty()) search = "";
            else searchFocused = true;
            scroll = 0;
            return true;
        }

        if (mx >= 574 && mx <= 686 && my >= 18 && my <= 48) {
            profileMenu = !profileMenu;
            settingsOpen = false;
            return true;
        }
        if (mx >= 696 && mx <= 726 && my >= 18 && my <= 48) {
            settingsOpen = !settingsOpen;
            settingsModule = null;
            profileMenu = false;
            return true;
        }

        if (profileMenu && mx >= 574 && mx <= 686 && my >= 52 && my <= 118) {
            int index = Math.max(0, Math.min(2, (my - 52) / 18));
            profile = new String[]{"Default", "Donut SMP", "Performance"}[index];
            profileMenu = false;
            loadProfile();
            saveProfile();
            return true;
        }

        if (settingsOpen && mx >= 476 && mx <= 742 && my >= 235 && my <= 361) {
            settingsOpen = false;
            return true;
        }
        if (settingsModule != null && mx >= 476 && mx <= 742 && my >= 235 && my <= 361) {
            if (my >= 300 && my <= 328) settingsModule.setPinned(!settingsModule.isPinned());
            else settingsModule = null;
            return true;
        }

        if (my >= 394 && my <= 428) {
            if (mx >= SIDEBAR_W + 18 && mx <= SIDEBAR_W + 238) {
                resetSettings();
                return true;
            }
            if (mx >= 540 && mx <= 742) {
                saveProfile();
                return true;
            }
        }

        List<Module> modules = visibleModules();
        int gridY = 124, cardW = 282, cardH = 43, gap = 7;
        int gridX = SIDEBAR_W + 18;
        int perPage = 10;
        int shownCount = Math.min(perPage, Math.max(0, modules.size() - scroll));
        for (int i = 0; i < shownCount; i++) {
            int cardX = gridX + (i % 2) * (cardW + gap);
            int cardY = gridY + (i / 2) * (cardH + 7);
            if (mx < cardX || mx > cardX + cardW || my < cardY || my > cardY + cardH) continue;
            Module module = modules.get(scroll + i);
            if (mx >= cardX + cardW - 83 && mx < cardX + cardW - 62) {
                module.setPinned(!module.isPinned());
                return true;
            }
            if (mx >= cardX + cardW - 63 && mx < cardX + cardW - 50) {
                settingsModule = module;
                settingsOpen = false;
                return true;
            }
            if (mx >= cardX + cardW - 50) {
                if (module.isImplemented()) module.toggle();
                settingsModule = module;
                return true;
            }
            if (event.button() == 1) {
                module.setPinned(!module.isPinned());
                return true;
            }
            if (module.isImplemented()) module.toggle();
            settingsModule = module;
            return true;
        }

        searchFocused = false;
        if (settingsModule != null || settingsOpen) {
            settingsModule = null;
            settingsOpen = false;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (searchFocused) {
            String typed = event.codepointAsString();
            if (!typed.isEmpty() && typed.codePoints().allMatch(cp -> !Character.isISOControl(cp))) {
                search += typed;
                scroll = 0;
            }
            return true;
        }
        return super.charTyped(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        int windowW = Math.min(BASE_W, width - 28);
        int windowH = Math.min(BASE_H, height - 28);
        int x = (width - windowW) / 2;
        int y = (height - windowH) / 2;
        if (mouseX >= x + SIDEBAR_W && mouseY >= y + 115 && mouseY <= y + 385) {
            int max = Math.max(0, visibleModules().size() - 10);
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