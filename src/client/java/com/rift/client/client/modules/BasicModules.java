package com.rift.client.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class BasicModules {
    private static boolean registered;
    private BasicModules() {}

    public static void register() {
        if (registered) return;
        registered = true;

        ModuleManager.register(new Module("Sprint", "Automatically maintains sprint while moving.", "Player") {
            @Override public void onTick() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null && player.input != null) player.setSprinting(true);
            }
            @Override public void onDisable() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) player.setSprinting(false);
            }
        });

        add("Coordinates", "Shows your current XYZ coordinates.", "Misc", true);
        add("FPS Display", "Shows the current frame rate.", "Render", true);
        add("Chunk Information", "Shows the chunk you are standing in.", "Donut", true);
        add("Totem Counter", "Counts totems in your inventory.", "Combat", true);
        add("Clock", "Displays your local time.", "Misc", true);
        add("Movement HUD", "Displays your current movement speed.", "Player", true);
        add("Combat HUD", "Displays a compact combat status panel.", "Combat", true);

        add("Sus Chunks", "Flags unusual terrain patterns in loaded chunks.", "Donut", false);
        add("Chunk Finder", "Inspects loaded chunk information.", "Donut", false);
        add("New Chunks", "Highlights candidates for recently generated chunks.", "Donut", false);
        add("Cave / Tunnel Finder", "Inspects nearby cave and tunnel geometry.", "Donut", false);
        add("Storage ESP", "Highlights storage blocks in loaded areas.", "Render", false);
        add("Block ESP", "Highlights selected block types.", "Render", false);
        add("Amethyst Finder", "Finds amethyst geode candidates.", "Donut", false);
        add("Ancient Debris Finder", "Finds ancient debris in loaded terrain.", "Donut", false);
        add("Spawner / Trial Chamber Finder", "Finds nearby structure indicators.", "Donut", false);
        add("Live Player Finder", "Displays players currently visible to the client.", "Donut", false);
        add("Sound Detector", "Displays nearby in-game sound events.", "Donut", false);
        add("Portal / Beacon Tracer", "Marks visible portals and beacons.", "Render", false);

        add("Player ESP", "Adds visual markers for visible players.", "Render", false);
        add("Entity ESP", "Adds visual markers for selected entities.", "Render", false);
        add("Target Renderer", "Displays information about a selected entity.", "Render", false);
        add("Friends List", "Stores player names marked as friends.", "Client", false);
        add("Anti-Bot", "Filters suspicious entity entries from client displays.", "Render", false);
        add("Item Culler", "Reduces dropped-item rendering.", "Render", false);
        add("Particle Reducer", "Reduces visual particle effects.", "Render", false);
        add("Low Animations", "Reduces selected interface animations.", "Render", false);
        add("Inventory HUD", "Displays a compact inventory panel.", "Render", false);
        add("Armor HUD", "Displays equipped armor and durability.", "Render", false);
        add("Potion HUD", "Displays active status effects.", "Render", false);
        add("Dropped Item Visibility", "Controls dropped-item visibility.", "Render", false);
        add("Freecam", "Local camera exploration mode.", "Player", false);
        add("Screenshot Manager", "Organizes screenshots from your session.", "Misc", false);

        add("Fly", "Client movement mode.", "Player", false);
        add("Scaffold", "Assisted block placement mode.", "Player", false);
        add("Bridge Assist", "Assists with careful bridging.", "Player", false);
        add("Safe Walk", "Helps prevent accidental edge movement.", "Player", false);
        add("Velocity", "Movement response settings.", "Player", false);
        add("Auto Jump Reset", "Movement timing helper.", "Player", false);
        add("No Jump Delay", "Adjusts local jump timing.", "Player", false);
        add("Fake Lag", "Network display and testing utility.", "Misc", false);
        add("Auto Potion", "Potion inventory convenience helper.", "Player", false);
        add("Auto XP", "Experience bottle inventory helper.", "Player", false);
        add("Fast XP", "Experience bottle convenience setting.", "Player", false);
        add("Pearl Macro", "Configurable item-use shortcut.", "Player", false);
        add("Auto Tool", "Selects a suitable hotbar tool.", "Player", false);
        add("Fast Place", "Block placement convenience setting.", "Player", false);
        add("Inventory Display", "Displays inventory contents.", "Misc", false);
        add("Server Information", "Shows current server details.", "Donut", false);
        add("Ping Display", "Shows connection latency.", "Misc", false);
        add("Keystrokes", "Displays movement key states.", "Misc", false);
        add("Statistics", "Displays personal session statistics.", "Donut", false);
        add("Donut HUD", "Combines useful Donut SMP information.", "Donut", false);

        add("Notifications", "Displays RIFT status notifications.", "Client", false);
        add("Profiles", "Stores named module configurations.", "Client", false);
        add("HUD Editor", "Customize the position of HUD elements.", "Client", false);
        add("Custom Theme", "Customize the RIFT color palette.", "Client", false);
        add("Module Search", "Search the module registry.", "Client", false);
        add("Custom Donut Profile", "A dedicated Donut SMP configuration.", "Donut", false);
    }

    private static void add(String name, String description, String category, boolean implemented) {
        Module module = new Module(name, description, category, implemented) {};
        if (!implemented) {
            module.setEnabled(false);
        }
        ModuleManager.register(module);
    }
}
