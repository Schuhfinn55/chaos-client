package com.onyx.visuals.module;

import net.minecraft.client.gui.DrawContext;

/**
 * Base class for draggable on-screen HUD elements.
 * Holds a screen position; rendered by RenderManager, moved in HudEditorScreen.
 * Position is persisted via ConfigManager.
 */
public abstract class HudModule extends Module {

    protected int posX;
    protected int posY;

    public HudModule(String name, String description, int defaultX, int defaultY) {
        super(name, description, Category.HUD);
        this.posX = defaultX;
        this.posY = defaultY;
    }

    public int getX() { return posX; }
    public int getY() { return posY; }

    public void setPos(int x, int y) {
        this.posX = Math.max(0, x);
        this.posY = Math.max(0, y);
    }

    public abstract int getHudWidth();
    public abstract int getHudHeight();
    public abstract void renderHud(DrawContext ctx);

    public boolean isInside(int mx, int my) {
        return mx >= posX && mx <= posX + getHudWidth()
                && my >= posY && my <= posY + getHudHeight();
    }
}
