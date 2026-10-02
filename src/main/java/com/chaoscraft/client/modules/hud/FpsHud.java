package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.EnumSetting;
import net.minecraft.client.gui.DrawContext;

/** FPS Counter: aktuell, min, max, Durchschnitt, Farbe nach FPS, Stil. */
public class FpsHud extends HudModule {

    public enum Style { MODERN, COMPACT, CLASSIC }

    private final BooleanSetting showMin = add(new BooleanSetting("Minimale FPS", "Niedrigsten Wert der letzten Sekunden anzeigen.", false));
    private final BooleanSetting showMax = add(new BooleanSetting("Maximale FPS", "Höchsten Wert anzeigen.", false));
    private final BooleanSetting showAvg = add(new BooleanSetting("Durchschnitt", "Durchschnitts-FPS anzeigen.", false));
    private final BooleanSetting colorByFps = add(new BooleanSetting("Farbe nach FPS", "Grün/Gelb/Rot je nach Bildrate.", true));
    private final EnumSetting<Style> style = add(new EnumSetting<>("Style", "Darstellung.", Style.MODERN));

    private int min = Integer.MAX_VALUE, max = 0;
    private long sum, samples, lastReset = System.currentTimeMillis();

    public FpsHud() {
        super("FPS Counter", "Zeigt deine Bildrate (FPS) in Echtzeit.", Category.HUD, "▲", 8, 8);
        tags("fps", "frames", "bildrate");
    }

    private void sample() {
        int fps = mc.getCurrentFps();
        min = Math.min(min, fps); max = Math.max(max, fps); sum += fps; samples++;
        if (System.currentTimeMillis() - lastReset > 10_000) { min = fps; max = fps; sum = fps; samples = 1; lastReset = System.currentTimeMillis(); }
    }

    private String line() {
        int fps = mc.getCurrentFps();
        return switch (style.get()) { case COMPACT -> fps + ""; case CLASSIC -> fps + " fps"; default -> "FPS: " + fps; };
    }

    private String extras() {
        StringBuilder sb = new StringBuilder();
        if (showMin.isEnabled()) sb.append("min ").append(min == Integer.MAX_VALUE ? 0 : min);
        if (showMax.isEnabled()) sb.append(sb.length() > 0 ? "  " : "").append("max ").append(max);
        if (showAvg.isEnabled()) sb.append(sb.length() > 0 ? "  " : "").append("avg ").append(samples == 0 ? 0 : sum / samples);
        return sb.toString();
    }

    private int fpsColor() {
        if (!colorByFps.isEnabled()) return color();
        int fps = mc.getCurrentFps();
        return fps >= 60 ? 0xFF4ADE80 : fps >= 30 ? 0xFFFBBF24 : 0xFFF87171;
    }

    @Override public int getContentWidth() { sample(); return Math.max(textWidth(line()), extras().isEmpty() ? 0 : textWidth(extras())); }
    @Override public int getContentHeight() { return extras().isEmpty() ? 10 : 20; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int w = getContentWidth();
        text(ctx, line(), x, y, w, fpsColor());
        String ex = extras();
        if (!ex.isEmpty()) text(ctx, ex, x, y + 10, w, textColor.withAlpha(0.7));
    }
}
