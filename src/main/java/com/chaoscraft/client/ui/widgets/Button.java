package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.ui.Anim;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

/** Button mit Hover-/Klick-Animation. Stile: PRIMARY (Akzent), DEFAULT, GHOST, DANGER. */
public class Button extends Widget {

    public enum Style { PRIMARY, DEFAULT, GHOST, DANGER }

    private String label;
    private final Runnable onClick;
    private Style style = Style.DEFAULT;
    private final Anim hover = new Anim(0f, 14f);
    private final Anim press = new Anim(0f, 20f);
    private boolean active;

    public Button(int x, int y, int w, int h, String label, Runnable onClick) {
        super(x, y, w, h);
        this.label = label;
        this.onClick = onClick;
    }

    public Button style(Style s) { this.style = s; return this; }
    public Button active(boolean a) { this.active = a; return this; }
    public void setLabel(String l) { this.label = l; }
    public String getLabel() { return label; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (!visible) return;
        boolean hov = enabled && hovered(mx, my);
        hover.setTarget(hov ? 1f : 0f);
        float hv = hover.get();
        float pv = press.get();
        if (pv > 0.95f) press.setTarget(0f);
        int r = Math.min(theme().radius(), h / 2);
        int bg, fg, border;
        switch (style) {
            case PRIMARY -> { bg = Draw.mix(theme().accentDark(), theme().accent(), 0.5f + hv * 0.5f); fg = 0xFFFFFFFF; border = theme().accent(); }
            case DANGER -> { bg = Draw.mix(0xFF2A1010, 0xFF5B1F1F, hv); fg = theme().danger(); border = Draw.mix(0xFF5B1F1F, theme().danger(), hv); }
            case GHOST -> { bg = Draw.alpha(theme().bg3(), (int) (hv * 255)); fg = Draw.mix(theme().textDim(), theme().text(), hv); border = 0; }
            default -> { bg = Draw.mix(theme().bg3(), theme().accentDark(), hv * 0.6f); fg = theme().text(); border = Draw.mix(theme().borderLight(), theme().accent(), hv); }
        }
        if (active) { bg = Draw.mix(bg, theme().accent(), 0.5f); border = theme().accent(); fg = 0xFFFFFFFF; }
        if (!enabled) { bg = Draw.alpha(bg, 90); fg = Draw.alpha(fg, 120); border = Draw.alpha(border, 90); }
        if (style == Style.PRIMARY && theme().shadows()) Draw.shadow(ctx, x, y, w, h, r, (int) (60 + hv * 50));
        int inset = (int) (pv * 1.5f);
        Draw.roundedRect(ctx, x + inset, y + inset, w - inset * 2, h - inset * 2, r, bg);
        if (border != 0) Draw.roundedBorder(ctx, x + inset, y + inset, w - inset * 2, h - inset * 2, r, border);
        String l = Draw.trim(label, w - 10);
        ctx.drawTextWithShadow(font(), l, x + (w - font().getWidth(l)) / 2, y + (h - 8) / 2, fg);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!enabled || button != 0 || !hovered(mx, my)) return false;
        press.snap(0f);
        press.setTarget(1f);
        playClick();
        if (onClick != null) onClick.run();
        return true;
    }
}
