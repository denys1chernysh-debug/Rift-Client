package com.rift.client.client;

import com.rift.client.client.modules.BasicModules;
import com.rift.client.client.modules.ModuleManager;
import com.rift.client.client.modules.EspRenderer;
import com.rift.client.client.ui.RiftHud;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class RIFTClientClient implements ClientModInitializer {
    private KeyMapping openClickGui;

    @Override
    public void onInitializeClient() {
        BasicModules.register();
        EspRenderer.register();
        openClickGui = KeyMappingHelper.registerKeyMapping(new KeyMapping(
            "key.rift.clickgui",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT_SHIFT,
            KeyMapping.Category.MISC
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openClickGui.consumeClick()) {
                if (client.gui.screen() == null) {
                    client.gui.setScreen(new RiftScreen());
                } else if (client.gui.screen() instanceof RiftScreen) {
                    client.gui.setScreen(null);
                }
            }
            ModuleManager.tick();
            EspRenderer.tick();
        });

        HudElementRegistry.addLast(
            Identifier.fromNamespaceAndPath("rift-client", "rift_hud"),
            RiftHud::extract
        );
    }
}
