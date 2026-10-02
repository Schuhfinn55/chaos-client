package com.chaoscraft.client.config;

import com.chaoscraft.client.ui.Draw;

/**
 * Globales Chaos-Theme für alle Ingame-GUIs: Dark/Light, Akzent (Chaos Red),
 * Skalierung, Transparenz, Radius, Schatten, Animationen, Blur-Stufe.
 * Werte werden vom {@code ChaosThemeModule} gesetzt und hier zentral gelesen.
 */
public final class ChaosTheme {

    private static final ChaosTheme INSTANCE = new ChaosTheme();
    public static ChaosTheme get() { return INSTANCE; }

    public enum Mode { DARK, LIGHT }

    private Mode mode = Mode.DARK;
    private int accent = 0xFFE11D2E;
    private float scale = 1.0f;
    private int opacity = 96;      // Prozent
    private int radius = 8;
    private boolean shadows = true;
    private boolean animations = true;
    private int blur = 2;          // 0..4 (Hintergrund-Abdunklung)

    private ChaosTheme() {}

    /* ---------- Setter (vom Theme-Modul) ---------- */
    public void apply(Mode mode, int accent, double scale, int opacity, int radius, boolean shadows, boolean animations, int blur) {
        this.mode = mode;
        this.accent = accent | 0xFF000000;
        this.scale = (float) scale;
        this.opacity = opacity;
        this.radius = radius;
        this.shadows = shadows;
        this.animations = animations;
        this.blur = blur;
    }

    /* ---------- Getter ---------- */
    public Mode mode() { return mode; }
    public boolean dark() { return mode == Mode.DARK; }
    public float scale() { return scale; }
    public int radius() { return radius; }
    public boolean shadows() { return shadows; }
    public boolean animations() { return animations; }
    public int blur() { return blur; }
    public float opacity() { return opacity / 100f; }

    public int accent() { return accent; }
    public int accentDark() { return Draw.brighten(accent, 0.65f); }
    public int accentLight() { return Draw.mix(accent, 0xFFFFFFFF, 0.3f); }
    public int accentGlow(int alpha) { return Draw.alpha(accent, alpha); }

    public int bg0() { return dark() ? Draw.alpha(0x09090B, (int) (255 * opacity())) : Draw.alpha(0xF3EFF0, (int) (255 * opacity())); }
    public int bg1() { return dark() ? Draw.alpha(0x111114, (int) (255 * opacity())) : Draw.alpha(0xFFFFFF, (int) (255 * opacity())); }
    public int bg2() { return dark() ? 0xFF17171B : 0xFFF7F3F4; }
    public int bg3() { return dark() ? 0xFF202026 : 0xFFEBE4E6; }
    public int border() { return dark() ? 0xFF2A2229 : 0xFFE2D8DC; }
    public int borderLight() { return dark() ? 0xFF3A2A30 : 0xFFD2C4C9; }
    public int text() { return dark() ? 0xFFF4F1F2 : 0xFF1B1518; }
    public int textDim() { return dark() ? 0xFFA69FA2 : 0xFF5D535A; }
    public int textFaint() { return dark() ? 0xFF6C6469 : 0xFF8E838A; }
    public int success() { return 0xFF4ADE80; }
    public int warning() { return 0xFFFBBF24; }
    public int danger() { return 0xFFF87171; }
    public int overlay() { return Draw.alpha(0x000000, 40 + blur * 45); }
}
