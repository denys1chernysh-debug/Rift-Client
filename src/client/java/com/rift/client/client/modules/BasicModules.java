package com.rift.client.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class BasicModules {
    private static boolean registered;
    private BasicModules() {}

    public static void register() {
        if (registered) return;
        registered = true;

        ModuleManager.register(new Module("Sprint", "Keeps sprint active while moving.", "Player") {
            @Override public void onTick() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null && player.input != null) player.setSprinting(true);
            }
            @Override public void onDisable() {
                LocalPlayer player = Minecraft.getInstance().player;
                if (player != null) player.setSprinting(false);
            }
        });

        // Working HUD modules
        add("Coordinates", "Shows your current XYZ coordinates.", "Visuals", true);
        add("FPS Display", "Shows the current frame rate.", "Visuals", true);
        add("Chunk Information", "Shows your current chunk coordinates.", "Donut", true);
        add("Totem Counter", "Counts totems in your inventory.", "Combat", true);
        add("Clock", "Displays your local time.", "Visuals", true);
        add("Movement HUD", "Displays your current movement speed.", "Player", true);
        add("Combat HUD", "Displays a compact combat status panel.", "Combat", true);

        // Donut SMP page — informational and terrain-inspection tools
        add("Sus Chunks", "Show suspicious chunks.", "Donut", false);
        add("Chunk Finder", "Inspect loaded chunk information.", "Donut", false);
        add("New Chunks", "Highlight candidate newly generated terrain.", "Donut", false);
        add("Cave / Tunnel Finder", "Inspect nearby cave and tunnel geometry.", "Donut", false);
        add("Amethyst Finder", "Find amethyst geode candidates in loaded terrain.", "Donut", false);
        add("Ancient Debris Finder", "Find ancient debris in loaded terrain.", "Donut", false);
        add("Spawner / Trial Chamber Finder", "Inspect nearby structure indicators.", "Donut", false);
        add("Live Player Finder", "Show information about visible players.", "Donut", false);
        add("Sound Detector", "Display nearby in-game sound events.", "Donut", false);
        add("Server Information", "Show current server details.", "Donut", false);
        add("Statistics", "Display session statistics.", "Donut", false);
        add("Donut HUD", "Combine useful Donut SMP information.", "Donut", false);
        add("Base Finder", "Inspect visible terrain for base indicators.", "Donut", false);
        add("Custom Donut Profile", "Dedicated Donut SMP module configuration.", "Donut", false);

        // Visual and render tools
        add("Block ESP", "Highlight selected block types.", "Visuals", false);
        add("Storage ESP", "Highlight storage blocks in loaded areas.", "Visuals", false);
        add("Player ESP", "Add visual markers for visible players.", "Visuals", false);
        add("Entity ESP", "Add visual markers for selected entities.", "Visuals", false);
        add("Target Renderer", "Display information about a selected entity.", "Visuals", false);
        add("Portal / Beacon Tracer", "Mark visible portals and beacons.", "Visuals", false);
        add("Item Culler", "Reduce dropped-item rendering.", "Visuals", false);
        add("Particle Reducer", "Reduce visual particle effects.", "Visuals", false);
        add("Low Animations", "Reduce selected interface animations.", "Visuals", false);
        add("Inventory HUD", "Display a compact inventory panel.", "Visuals", false);
        add("Armor HUD", "Display equipped armor and durability.", "Visuals", false);
        add("Potion HUD", "Display active status effects.", "Visuals", false);
        add("Dropped Item Visibility", "Control dropped-item visibility.", "Visuals", false);
        add("Keystrokes", "Display movement key states.", "Visuals", false);
        add("Ping Display", "Show connection latency.", "Visuals", false);

        // Movement and inventory utilities
        add("Freecam", "Local camera exploration mode.", "Player", false);
        add("Safe Walk", "Help prevent accidental edge movement.", "Player", false);
        add("Auto Tool", "Select a suitable hotbar tool.", "Player", false);
        add("Auto Potion", "Potion inventory convenience helper.", "Player", false);
        add("Auto XP", "Experience bottle inventory helper.", "Player", false);
        add("Fast XP", "Experience bottle convenience setting.", "Player", false);
        add("Pearl Macro", "Configurable item-use shortcut.", "Player", false);
        add("Fast Place", "Block placement convenience setting.", "Player", false);
        add("Inventory Display", "Display inventory contents.", "Player", false);
        add("Fly", "Client movement mode.", "Player", false);
        add("Scaffold", "Assisted block placement mode.", "Player", false);
        add("Bridge Assist", "Assists with careful bridging.", "Player", false);
        add("Velocity", "Movement response settings.", "Player", false);
        add("No Jump Delay", "Adjust local jump timing.", "Player", false);

        // Combat status and kit information (no automated attacks)
        add("Armor Status", "Show armor and durability status.", "Combat", false);
        add("Totem Status", "Show offhand and inventory totem status.", "Combat", false);
        add("Cooldown HUD", "Display item and attack cooldown information.", "Combat", false);
        add("Crystal Status", "Display crystal-related inventory information.", "Crystal", false);
        add("Anchor Status", "Display respawn-anchor inventory information.", "Crystal", false);
        add("Mace Status", "Display mace and wind-charge inventory information.", "Mace", false);
        add("Cart Status", "Display minecart inventory information.", "Mace", false);
        add("Spear Status", "Display spear inventory information.", "Mace", false);

        // Client tools
        add("Friends List", "Store player names marked as friends.", "Client", false);
        add("Notifications", "Display RIFT status notifications.", "Client", false);
        add("Profiles", "Store named module configurations.", "Client", false);
        add("HUD Editor", "Customize the position of HUD elements.", "Client", false);
        add("Custom Theme", "Customize the RIFT color palette.", "Client", false);
        add("Module Search", "Search the module registry.", "Client", false);
        add("Screenshot Manager", "Organize screenshots from your session.", "Client", false);
        add("Anti-Bot", "Filter suspicious entity entries from client displays.", "Client", false);
    }

    private static void add(String name, String description, String category, boolean implemented) {
        Module module = new Module(name, description, category, implemented) {};
        ModuleManager.register(module);
    }
}