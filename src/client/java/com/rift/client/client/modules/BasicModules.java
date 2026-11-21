
package com.rift.client.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public class BasicModules {

    private static boolean registered = false;

    public static void register() {
        if (registered) return;
        registered = true;

        ModuleManager.register(new Module(
            "Coordinates",
            "Displays your current coordinates",
            "Misc"
        ) {
            @Override
            public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                if (mc.player == null) return;

                // Coordinates are read by the HUD.
            }
        });

        ModuleManager.register(new Module(
            "FPS Display",
            "Displays your current FPS",
            "Render"
        ));

        ModuleManager.register(new Module(
            "Sprint",
            "Automatically keeps sprint active",
            "Player"
        ) {
            @Override
            public void onTick() {
                Minecraft mc = Minecraft.getInstance();
                LocalPlayer player = mc.player;

                if (player != null && player.input != null) {
                    player.setSprinting(true);
                }
            }
        });

        ModuleManager.register(new Module(
            "Particle Reducer",
            "Reduces visual particle effects",
            "Render"
        ));

        ModuleManager.register(new Module(
            "Chunk Information",
            "Displays information about your current chunk",
            "Donut"
        ));

        ModuleManager.register(new Module(
            "Totem Counter",
            "Displays your carried totems",
            "Combat"
        ));
    }
}
