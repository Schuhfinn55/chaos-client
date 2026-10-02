package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.HudModule;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;

/**
 * FpsPing — shows current FPS and server ping. Draggable HUD element.
 * Yarn mappings 1.21.11.
 */
public class FpsPingModule extends HudModule {

    public FpsPingModule() {
        super("FPS & Ping", "Show FPS and server latency.", 8, 8);
    }

    private String text() {
        int fps = mc.getCurrentFps();
        int ping = -1;
        if (mc.getNetworkHandler() != null && mc.player != null) {
            PlayerListEntry entry = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            if (entry != null) ping = entry.getLatency();
        }
        return fps + " FPS" + (ping >= 0 ? "  |  " + ping + " ms" : "");
    }

    @Override
    public int getHudWidth() { return mc.textRenderer.getWidth(text()) + 6; }

    @Override
    public int getHudHeight() { return 12; }

    @Override
    public void renderHud(DrawContext ctx) {
        String text = text();
        ctx.fill(posX - 2, posY - 2, posX + mc.textRenderer.getWidth(text) + 4, posY + 10, 0x99000000);
        ctx.drawTextWithShadow(mc.textRenderer, text, posX + 1, posY, 0xFF00E676);
    }
}
