package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.config.ChaosTheme;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

/** Basis aller Chaos-UI-Widgets (eigenes, leichtgewichtiges Widget-System). */
public abstract class Widget {

    protected static final MinecraftClient mc = MinecraftClient.getInstance();

    public int x, y, w, h;
    public boolean visible = true;
    public boolean enabled = true;
    protected String tooltip;

    protected Widget(int x, int y, int w, int h) { this.x = x; this.y = y; this.w = w; this.h = h; }

    public Widget tooltip(String t) { this.tooltip = t; return this; }
    public String getTooltip() { return tooltip; }

    public boolean hovered(double mx, double my) { return visible && Draw.in(mx, my, x, y, w, h); }

    public abstract void render(DrawContext ctx, int mx, int my, float delta);

    /** Für Popups (Dropdowns), die über allem gezeichnet werden. */
    public void renderOverlay(DrawContext ctx, int mx, int my) {}
    public boolean hasOverlay() { return false; }

    public boolean mouseClicked(double mx, double my, int button) { return false; }
    public boolean mouseReleased(double mx, double my, int button) { return false; }
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) { return false; }
    public boolean mouseScrolled(double mx, double my, double amount) { return false; }
    public boolean keyPressed(KeyInput key) { return false; }
    public boolean charTyped(CharInput chr) { return false; }
    public boolean isFocused() { return false; }
    public void setFocused(boolean f) {}

    protected ChaosTheme theme() { return ChaosTheme.get(); }
    protected TextRenderer font() { return mc.textRenderer; }
    protected void playClick() {
        try {
            float vol = com.chaoscraft.client.ChaosClient.get().getAudio().uiVolume();
            if (vol > 0) mc.getSoundManager().play(net.minecraft.client.sound.PositionedSoundInstance.ui(net.minecraft.sound.SoundEvents.UI_BUTTON_CLICK.value(), 1.4f, vol * 0.6f));
        } catch (Exception ignored) {}
    }
}
