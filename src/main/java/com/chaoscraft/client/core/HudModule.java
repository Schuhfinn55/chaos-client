package com.chaoscraft.client.core;

import com.chaoscraft.client.config.ChaosTheme;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.DoubleSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

/**
 * Basis für verschiebbare HUD-Elemente. Position wird gespeichert; Skalierung,
 * Rotation, Transparenz, Ausrichtung, Abstände, Farbe, Hintergrund, Rahmen,
 * Eckenradius und Schatten sind gemeinsame Einstellungen jedes HUD-Moduls.
 *
 * Unterklassen implementieren {@link #getContentWidth()}, {@link #getContentHeight()}
 * und {@link #renderContent(DrawContext, int, int)} in unskalierten Koordinaten.
 */
public abstract class HudModule extends Module {

    public enum Align { LEFT, CENTER, RIGHT }

    protected int posX;
    protected int posY;

    protected final DoubleSetting scale = add(new DoubleSetting("Scale", "Größe des Elements.", 1.0, 0.5, 3.0, 0.05));
    protected final IntSetting opacity = add(new IntSetting("Opacity", "Deckkraft in Prozent.", 100, 10, 100));
    protected final IntSetting rotation = add(new IntSetting("Rotation", "Drehung in Grad.", 0, 0, 359));
    protected final EnumSetting<Align> align = add(new EnumSetting<>("Alignment", "Textausrichtung im Element.", Align.LEFT));
    protected final IntSetting padding = add(new IntSetting("Padding", "Innenabstand in Pixeln.", 3, 0, 12));
    protected final ColorSetting textColor = add(new ColorSetting("Text Color", "Textfarbe.", 0xFFFFFFFF, false));
    protected final BooleanSetting textShadow = add(new BooleanSetting("Text Shadow", "Schatten unter dem Text.", true));
    protected final BooleanSetting background = add(new BooleanSetting("Background", "Hintergrund zeichnen.", true));
    protected final ColorSetting backgroundColor = add(new ColorSetting("Background Color", "Farbe des Hintergrunds.", 0x99000000));
    protected final BooleanSetting border = add(new BooleanSetting("Border", "Rahmen zeichnen.", false));
    protected final ColorSetting borderColor = add(new ColorSetting("Border Color", "Farbe des Rahmens.", 0xFFE11D2E));
    protected final IntSetting radius = add(new IntSetting("Corner Radius", "Eckenradius des Hintergrunds.", 4, 0, 10));
    protected final BooleanSetting shadow = add(new BooleanSetting("Shadow", "Weicher Schatten hinter dem Element.", false));

    protected HudModule(String name, String description, Category category, int defaultX, int defaultY) {
        this(name, description, category, "", defaultX, defaultY);
    }

    protected HudModule(String name, String description, Category category, String icon, int defaultX, int defaultY) {
        super(name, description, category, icon);
        this.posX = defaultX;
        this.posY = defaultY;
        tags("hud", "anzeige", "overlay");
    }

    /* ---------- Position ---------- */
    public int getX() { return posX; }
    public int getY() { return posY; }
    public void setPos(int x, int y) { this.posX = Math.max(0, x); this.posY = Math.max(0, y); }

    /** Breite/Höhe auf dem Bildschirm (skaliert, inkl. Padding). */
    public int getHudWidth() { return (int) Math.ceil((getContentWidth() + padding.getInt() * 2) * scale.get()); }
    public int getHudHeight() { return (int) Math.ceil((getContentHeight() + padding.getInt() * 2) * scale.get()); }
    public float getScale() { return scale.getFloat(); }

    public boolean isInside(int mx, int my) {
        return mx >= posX && mx <= posX + getHudWidth() && my >= posY && my <= posY + getHudHeight();
    }

    /* ---------- Rendering ---------- */
    /** Inhaltsbreite in unskalierten Pixeln (ohne Padding). */
    public abstract int getContentWidth();
    /** Inhaltshöhe in unskalierten Pixeln (ohne Padding). */
    public abstract int getContentHeight();
    /** Zeichnet den Inhalt ab (x, y) in unskalierten Koordinaten. */
    public abstract void renderContent(DrawContext ctx, int x, int y);

    /** Zeichnet Rahmen/Hintergrund + Inhalt an der gespeicherten Position. */
    public void renderHud(DrawContext ctx) {
        float s = scale.getFloat();
        int pad = padding.getInt();
        int cw = getContentWidth() + pad * 2;
        int ch = getContentHeight() + pad * 2;
        float alpha = opacity.getInt() / 100f;

        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(posX, posY);
        if (rotation.getInt() != 0) {
            m.translate(cw * s / 2f, ch * s / 2f);
            m.rotate((float) Math.toRadians(rotation.getInt()));
            m.translate(-cw * s / 2f, -ch * s / 2f);
        }
        m.scale(s, s);
        if (shadow.isEnabled()) Draw.shadow(ctx, 0, 0, cw, ch, radius.getInt(), (int) (90 * alpha));
        if (background.isEnabled()) Draw.roundedRect(ctx, 0, 0, cw, ch, radius.getInt(), Draw.withAlpha(backgroundColor.argb(), alpha));
        if (border.isEnabled()) Draw.roundedBorder(ctx, 0, 0, cw, ch, radius.getInt(), Draw.withAlpha(borderColor.argb(), alpha));
        renderContent(ctx, pad, pad);
        m.popMatrix();
    }

    /* ---------- Hilfen für Unterklassen ---------- */
    protected int color() { return textColor.argb(); }
    protected int accent() { return ChaosTheme.get().accent(); }

    /** Text gemäß Ausrichtung in einer Zeile der Breite w zeichnen. */
    protected void text(DrawContext ctx, String s, int x, int y, int w) { text(ctx, s, x, y, w, color()); }
    protected void text(DrawContext ctx, String s, int x, int y, int w, int col) {
        int tw = mc.textRenderer.getWidth(s);
        int tx = switch (align.get()) { case CENTER -> x + (w - tw) / 2; case RIGHT -> x + w - tw; default -> x; };
        ctx.drawText(mc.textRenderer, s, tx, y, col, textShadow.isEnabled());
    }
    protected int textWidth(String s) { return mc.textRenderer.getWidth(s); }
    protected int lineHeight() { return mc.textRenderer.fontHeight + 2; }
}
