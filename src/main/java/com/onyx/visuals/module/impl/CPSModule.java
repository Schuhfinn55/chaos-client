package com.onyx.visuals.module.impl;

import com.onyx.visuals.module.HudModule;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/**
 * CPS — counts left-clicks per second. CpsMixin calls recordClick() on
 * every attack click. Draggable HUD element.
 * Yarn mappings 1.21.11.
 */
public class CPSModule extends HudModule {

    private final List<Long> clicks = new ArrayList<>();

    public CPSModule() {
        super("CPS", "Show clicks per second.", 8, 24);
    }

    /** Called from CpsMixin on every attack click. */
    public void recordClick() {
        clicks.add(System.currentTimeMillis());
    }

    public int getCps() {
        long now = System.currentTimeMillis();
        clicks.removeIf(t -> now - t > 1000);
        return clicks.size();
    }

    private String text() { return getCps() + " CPS"; }

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
