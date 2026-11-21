package com.rift.client.client;

import com.rift.client.client.modules.BasicModules;
import com.rift.client.client.modules.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

public class RIFTClientClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        BasicModules.register();
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            ModuleManager.tick();
        });
    }
}
