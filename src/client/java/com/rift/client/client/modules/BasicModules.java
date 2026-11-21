package com.rift.client.client.modules;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;

public final class BasicModules {
    private static boolean registered;
    private BasicModules() {}

    public static void register() {
        if (registered) return;
        registered = true;
        ModuleManager.register(new Module("Sprint","Automatically maintains sprint while moving","Player") {
            @Override public void onTick() {
                LocalPlayer p=Minecraft.getInstance().player;
                if(p!=null&&p.input!=null) p.setSprinting(true);
            }
        });
        ModuleManager.register(new Module("Coordinates","Coordinate data for the RIFT HUD","Misc"));
        ModuleManager.register(new Module("FPS Display","FPS data for the RIFT HUD","Render"));
        ModuleManager.register(new Module("Totem Counter","Totem count data for the RIFT HUD","Combat"));
        ModuleManager.register(new Module("Chunk Information","Current chunk information","Donut"));
        ModuleManager.register(new Module("Particle Reducer","Particle reduction settings","Render"));
    }
}
