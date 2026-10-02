package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

/**
 * Farbwähler: Vorschau + Hex, Preset-Reihe, Farbton-Streifen und
 * R/G/B(/A)-Slider – alles inline.
 */
public class ColorWidget extends Widget {

    private static final int[] PRESETS = {0xFFE11D2E, 0xFFFF5C6C, 0xFF8F1B22, 0xFFFFFFFF, 0xFF000000, 0xFFF97316, 0xFFFCD34D, 0xFF4ADE80, 0xFF22D3EE, 0xFF60A5FA, 0xFFA78BFA, 0xFFF472B6};
    private final ColorSetting setting;
    private int dragChannel = -1; // 0..3 = RGBA, 4 = Hue

    public ColorWidget(int x, int y, int w, ColorSetting setting) {
        super(x, y, w, setting.allowsAlpha() ? 62 : 52);
        this.setting = setting;
    }

    private int sliderY(int ch) { return y + 30 + ch * 10; }
    private int sliderX() { return x + 30; }
    private int sliderW() { return w - 30; }
    private int hueY() { return y + 20; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (!visible) return;
        // Vorschau + Hex
        Draw.roundedRect(ctx, x, y, 22, 16, 4, 0xFF000000 | setting.rgb());
        Draw.roundedBorder(ctx, x, y, 22, 16, 4, theme().borderLight());
        ctx.drawTextWithShadow(font(), setting.hex(), x + 28, y + 4, theme().textDim());
        // Presets
        int px = x + 80;
        for (int i = 0; i < PRESETS.length && px + 10 <= x + w; i++, px += 12) {
            boolean sel = (PRESETS[i] & 0xFFFFFF) == setting.rgb();
            Draw.roundedRect(ctx, px, y + 3, 10, 10, 3, PRESETS[i]);
            if (sel) Draw.roundedBorder(ctx, px - 1, y + 2, 12, 12, 4, 0xFFFFFFFF);
        }
        // Farbton-Streifen
        int hx = sliderX(), hw = sliderW();
        for (int i = 0; i < hw; i++) ctx.fill(hx + i, hueY(), hx + i + 1, hueY() + 6, Draw.hsv(360f * i / hw, 1f, 1f));
        ctx.drawTextWithShadow(font(), "Hue", x, hueY() - 1, theme().textFaint());
        // Kanäle
        String[] names = {"R", "G", "B", "A"};
        int[] vals = {setting.red(), setting.green(), setting.blue(), setting.alpha()};
        int[] cols = {0xFFFF5555, 0xFF55FF55, 0xFF5599FF, 0xFFCCCCCC};
        int channels = setting.allowsAlpha() ? 4 : 3;
        for (int c = 0; c < channels; c++) {
            int sy = sliderY(c);
            ctx.drawTextWithShadow(font(), names[c] + " " + vals[c], x, sy - 1, theme().textFaint());
            Draw.roundedRect(ctx, sliderX(), sy + 1, sliderW(), 4, 2, theme().bg3());
            int fill = (int) (sliderW() * vals[c] / 255f);
            Draw.roundedRect(ctx, sliderX(), sy + 1, Math.max(2, fill), 4, 2, cols[c]);
        }
    }

    private boolean handle(double mx, double my, boolean press) {
        int channels = setting.allowsAlpha() ? 4 : 3;
        if (press) {
            // Presets
            int px = x + 80;
            for (int i = 0; i < PRESETS.length && px + 10 <= x + w; i++, px += 12) {
                if (Draw.in(mx, my, px, y + 3, 10, 10)) { setting.setRgba((PRESETS[i] >> 16) & 0xFF, (PRESETS[i] >> 8) & 0xFF, PRESETS[i] & 0xFF, setting.alpha()); return true; }
            }
            if (Draw.in(mx, my, sliderX(), hueY() - 2, sliderW(), 10)) dragChannel = 4;
            for (int c = 0; c < channels; c++) if (Draw.in(mx, my, sliderX(), sliderY(c) - 3, sliderW(), 10)) dragChannel = c;
        }
        if (dragChannel < 0) return false;
        double p = Math.max(0, Math.min(1, (mx - sliderX()) / Math.max(1, sliderW())));
        if (dragChannel == 4) {
            int c = Draw.hsv((float) (p * 359.9), 1f, 1f);
            setting.setRgba((c >> 16) & 0xFF, (c >> 8) & 0xFF, c & 0xFF, setting.alpha());
        } else {
            setting.setChannel(dragChannel, (int) Math.round(p * 255));
        }
        return true;
    }

    @Override public boolean mouseClicked(double mx, double my, int button) {
        if (!enabled || button != 0 || !hovered(mx, my)) return false;
        return handle(mx, my, true) || true;
    }
    @Override public boolean mouseDragged(double mx, double my, int button, double dx, double dy) { return dragChannel >= 0 && handle(mx, my, false); }
    @Override public boolean mouseReleased(double mx, double my, int button) { boolean was = dragChannel >= 0; dragChannel = -1; return was; }
}
