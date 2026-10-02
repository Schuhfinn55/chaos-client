package com.chaoscraft.client.hud;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.modules.pvp.CrosshairModule;
import com.chaoscraft.client.ui.screens.HudEditorScreen;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.util.Identifier;

/**
 * Zeichnet alle aktiven HUD-Module, das Crosshair und die Notifications.
 * Im HUD-Editor übernimmt der Editor-Screen das Zeichnen selbst.
 */
public final class HudRenderer {

    private static final Identifier HUD_ID = Identifier.of("chaosclient", "hud");
    private static long lastFrame = System.nanoTime();
    private static float frameMs = 16f;

    private HudRenderer() {}

    public static void init() {
        HudElementRegistry.attachElementAfter(VanillaHudElements.MISC_OVERLAYS, HUD_ID, new ChaosHud());
    }

    /** Letzte Frametime in ms (für Performance-Center). */
    public static float frameMs() { return frameMs; }

    static final class ChaosHud implements HudElement {
        @Override
        public void render(DrawContext ctx, RenderTickCounter tickCounter) {
            long now = System.nanoTime();
            frameMs = frameMs * 0.9f + ((now - lastFrame) / 1_000_000f) * 0.1f;
            lastFrame = now;

            ChaosClient cc = ChaosClient.get();
            if (cc == null || cc.getModuleManager() == null) return;
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null || mc.world == null) return;
            boolean editor = mc.currentScreen instanceof HudEditorScreen;
            if (!mc.options.hudHidden && !editor) {
                for (Module mod : cc.getModuleManager().getModules()) {
                    if (!mod.isEnabled()) continue;
                    try {
                        if (mod instanceof HudModule hud) hud.renderHud(ctx);
                        else if (mod instanceof CrosshairModule ch) ch.render(ctx);
                    } catch (Exception e) {
                        ChaosClient.LOGGER.warn("[ChaosClient] HUD-Fehler in {}: {}", mod.getName(), e.toString());
                    }
                }
            }
            cc.getNotifications().render(ctx);
        }
    }
}
