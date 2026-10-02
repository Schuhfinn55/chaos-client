package com.chaoscraft.client.ui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

/**
 * Zeichenhilfen für das Chaos-UI: abgerundete Rechtecke, Rahmen, Schatten,
 * Verläufe, Farben. Nur {@link DrawContext#fill} – performant und
 * unabhängig von Texturen.
 */
public final class Draw {

    private Draw() {}

    public static int withAlpha(int argb, double alpha) {
        int a = (int) Math.round(((argb >>> 24) & 0xFF) * Math.max(0, Math.min(1, alpha)));
        return (a << 24) | (argb & 0xFFFFFF);
    }

    public static int alpha(int argb, int a) { return ((a & 0xFF) << 24) | (argb & 0xFFFFFF); }

    public static int mix(int c1, int c2, float t) {
        t = Math.max(0, Math.min(1, t));
        int a = (int) (((c1 >>> 24) & 0xFF) * (1 - t) + ((c2 >>> 24) & 0xFF) * t);
        int r = (int) (((c1 >> 16) & 0xFF) * (1 - t) + ((c2 >> 16) & 0xFF) * t);
        int g = (int) (((c1 >> 8) & 0xFF) * (1 - t) + ((c2 >> 8) & 0xFF) * t);
        int b = (int) ((c1 & 0xFF) * (1 - t) + (c2 & 0xFF) * t);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    public static int brighten(int argb, float f) {
        int a = (argb >>> 24) & 0xFF;
        int r = Math.min(255, (int) (((argb >> 16) & 0xFF) * f));
        int g = Math.min(255, (int) (((argb >> 8) & 0xFF) * f));
        int b = Math.min(255, (int) ((argb & 0xFF) * f));
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    /** HSV → ARGB (h 0..360, s/v 0..1). */
    public static int hsv(float h, float s, float v) {
        float c = v * s;
        float x = c * (1 - Math.abs((h / 60f) % 2 - 1));
        float m = v - c;
        float r, g, b;
        if (h < 60) { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else { r = c; g = 0; b = x; }
        return 0xFF000000 | ((int) ((r + m) * 255) << 16) | ((int) ((g + m) * 255) << 8) | (int) ((b + m) * 255);
    }

    /** Abgerundetes Rechteck. */
    public static void roundedRect(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) { ctx.fill(x, y, x + w, y + h, color); return; }
        // Mittelteil
        ctx.fill(x, y + r, x + w, y + h - r, color);
        // Ecken zeilenweise
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int dx = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            ctx.fill(x + dx, y + i, x + w - dx, y + i + 1, color);
            ctx.fill(x + dx, y + h - i - 1, x + w - dx, y + h - i, color);
        }
    }

    /** Rahmen (1px) um ein abgerundetes Rechteck. */
    public static void roundedBorder(DrawContext ctx, int x, int y, int w, int h, int r, int color) {
        if (w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) {
            ctx.fill(x, y, x + w, y + 1, color);
            ctx.fill(x, y + h - 1, x + w, y + h, color);
            ctx.fill(x, y, x + 1, y + h, color);
            ctx.fill(x + w - 1, y, x + w, y + h, color);
            return;
        }
        ctx.fill(x + r, y, x + w - r, y + 1, color);
        ctx.fill(x + r, y + h - 1, x + w - r, y + h, color);
        ctx.fill(x, y + r, x + 1, y + h - r, color);
        ctx.fill(x + w - 1, y + r, x + w, y + h - r, color);
        for (int i = 0; i < r; i++) {
            double dy = r - i - 0.5;
            int dx = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy)));
            double dy2 = r - i - 1.5;
            int dx2 = i + 1 < r ? (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy2 * dy2))) : r;
            int from = dx, to = Math.max(dx + 1, dx2);
            ctx.fill(x + from, y + i, x + to, y + i + 1, color);
            ctx.fill(x + w - to, y + i, x + w - from, y + i + 1, color);
            ctx.fill(x + from, y + h - i - 1, x + to, y + h - i, color);
            ctx.fill(x + w - to, y + h - i - 1, x + w - from, y + h - i, color);
        }
    }

    /** Weicher Schatten (mehrere Lagen mit abnehmender Deckkraft). */
    public static void shadow(DrawContext ctx, int x, int y, int w, int h, int r, int maxAlpha) {
        int layers = 4;
        for (int i = layers; i >= 1; i--) {
            int a = Math.max(0, maxAlpha / (layers + 1) * (layers - i + 1) / 2);
            roundedRect(ctx, x - i, y - i + 2, w + i * 2, h + i * 2, r + i, alpha(0x000000, a));
        }
    }

    /** Vertikaler Verlauf in einem abgerundeten Rechteck (zeilenweise). */
    public static void roundedGradient(DrawContext ctx, int x, int y, int w, int h, int r, int top, int bottom) {
        for (int i = 0; i < h; i++) {
            int c = mix(top, bottom, (float) i / Math.max(1, h - 1));
            int dx = 0;
            if (i < r) { double dy = r - i - 0.5; dx = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy))); }
            else if (i >= h - r) { double dy = r - (h - i) + 0.5; dx = (int) Math.round(r - Math.sqrt(Math.max(0, r * r - dy * dy))); }
            ctx.fill(x + dx, y + i, x + w - dx, y + i + 1, c);
        }
    }

    /** Toggle-Schalter (Pille) mit Knopf, t = Animationsfortschritt 0..1. */
    public static void toggle(DrawContext ctx, int x, int y, int w, int h, float t, int onColor, int offColor) {
        int track = mix(offColor, onColor, t);
        roundedRect(ctx, x, y, w, h, h / 2, track);
        int knob = h - 4;
        int kx = x + 2 + Math.round((w - h) * t);
        roundedRect(ctx, kx, y + 2, knob, knob, knob / 2, 0xFFFFFFFF);
    }

    public static void centeredText(DrawContext ctx, String s, int cx, int y, int color, boolean shadow) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        ctx.drawText(tr, s, cx - tr.getWidth(s) / 2, y, color, shadow);
    }

    public static String trim(String s, int maxWidth) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;
        if (tr.getWidth(s) <= maxWidth) return s;
        String t = tr.trimToWidth(s, Math.max(0, maxWidth - tr.getWidth("…")));
        return t + "…";
    }

    public static boolean in(double mx, double my, int x, int y, int w, int h) {
        return mx >= x && mx < x + w && my >= y && my < y + h;
    }
}
