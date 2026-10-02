package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.settings.DoubleSetting;
import com.chaoscraft.client.ui.Anim;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

/** Slider für DoubleSetting/IntSetting mit Wertanzeige und Drag. */
public class SliderWidget extends Widget {

    private final DoubleSetting setting;
    private boolean dragging;
    private final Anim knob = new Anim(0f, 18f);

    public SliderWidget(int x, int y, int w, DoubleSetting setting) {
        super(x, y, w, 14);
        this.setting = setting;
        knob.snap((float) setting.getProgress());
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (!visible) return;
        knob.setTarget((float) setting.getProgress());
        float p = knob.get();
        int trackY = y + h / 2 - 2;
        Draw.roundedRect(ctx, x, trackY, w, 4, 2, theme().bg3());
        int fill = Math.max(4, (int) (w * p));
        Draw.roundedRect(ctx, x, trackY, fill, 4, 2, enabled ? theme().accent() : Draw.alpha(theme().accent(), 110));
        int kx = x + (int) ((w - 8) * p);
        boolean hov = hovered(mx, my) || dragging;
        if (hov && theme().shadows()) Draw.roundedRect(ctx, kx - 2, trackY - 4, 12, 12, 6, theme().accentGlow(70));
        Draw.roundedRect(ctx, kx, trackY - 2, 8, 8, 4, 0xFFFFFFFF);
    }

    private void updateFromMouse(double mx) {
        setting.setFromProgress((mx - x) / Math.max(1, w));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!enabled || button != 0 || !hovered(mx, my)) return false;
        dragging = true;
        updateFromMouse(mx);
        return true;
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (!dragging) return false;
        updateFromMouse(mx);
        return true;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        if (!dragging) return false;
        dragging = false;
        return true;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (!enabled || !hovered(mx, my)) return false;
        setting.set(setting.get() + (amount > 0 ? setting.getStep() : -setting.getStep()));
        return true;
    }
}
