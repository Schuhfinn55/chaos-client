package com.onyx.visuals.render;

import com.onyx.visuals.OnyxVisuals;
import com.onyx.visuals.module.HudModule;
import com.onyx.visuals.module.Module;
import com.onyx.visuals.module.impl.CrosshairModule;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

/**
 * Registers a HUD element that draws all Onyx Visuals overlays.
 * Draggable HudModules render at their stored position; fixed modules
 * (Crosshair) render as before.
 * Yarn mappings 1.21.11.
 */
public class RenderManager {

    private static final Identifier HUD_ID = Identifier.of("chaosclient", "hud");

    public static void init() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, HUD_ID, new OnyxHud());
    }

    static class OnyxHud implements HudElement {
        @Override
        public void render(DrawContext ctx, RenderTickCounter tickCounter) {
            OnyxVisuals ov = OnyxVisuals.getInstance();
            if (ov == null || ov.getModuleManager() == null) return;

            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null || mc.options.hudHidden) return;

            for (Module mod : ov.getModuleManager().getModules()) {
                if (!mod.isEnabled()) continue;
                if (mod instanceof HudModule hud) {
                    hud.renderHud(ctx);
                } else if (mod instanceof CrosshairModule ch) {
                    ch.render(ctx);
                }
            }
        }
    }
}
