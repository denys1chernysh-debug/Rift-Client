package com.rift.client.client.modules;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ClientUtilities {
    private static final Set<String> FRIENDS = new LinkedHashSet<>();
    private static boolean loaded;
    private static boolean antiBot;
    private ClientUtilities() {}

    public static void refreshAntiBot() { antiBot = true; }
    public static boolean isAntiBotEnabled() { return antiBot; }
    public static boolean isLikelyBot(String name) {
        if (name == null || name.isBlank()) return true;
        String value = name.toLowerCase(Locale.ROOT);
        return value.startsWith("npc") || value.startsWith("bot") || value.contains("[npc]")
            || value.contains("dummy") || value.equals("shop") || value.equals("trader");
    }
    public static boolean isFriend(String name) { load(); return FRIENDS.contains(name.toLowerCase(Locale.ROOT)); }

    public static void toggleFriend(String name) {
        load();
        String key = name.toLowerCase(Locale.ROOT);
        if (!FRIENDS.add(key)) FRIENDS.remove(key);
        save();
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) mc.gui.hud.setOverlayMessage(
            Component.literal("RIFT friend " + (FRIENDS.contains(key) ? "added: " : "removed: ") + name), true);
    }

    private static void load() {
        if (loaded) return;
        loaded = true;
        Path file = file();
        try {
            if (Files.exists(file)) {
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    String name = line.trim().toLowerCase(Locale.ROOT);
                    if (!name.isEmpty()) FRIENDS.add(name);
                }
            }
        } catch (IOException ignored) {}
    }

    private static void save() {
        try {
            Path file = file();
            Files.createDirectories(file.getParent());
            Files.write(file, FRIENDS, StandardCharsets.UTF_8);
        } catch (IOException ignored) {}
    }

    private static Path file() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve("config").resolve("rift-friends.txt");
    }
}
