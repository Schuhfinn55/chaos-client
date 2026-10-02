package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.ui.Anim;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

/** Animierter Toggle-Schalter. */
public class ToggleWidget extends Widget {

    private final BooleanSupplier getter;
    private final Consumer<Boolean> setter;
    private final Anim anim;

    public ToggleWidget(int x, int y, BooleanSupplier getter, Consumer<Boolean> setter) {
        super(x, y, 30, 16);
        this.getter = getter;
        this.setter = setter;
        this.anim = new Anim(getter.getAsBoolean() ? 1f : 0f, 16f);
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (!visible) return;
        anim.setTarget(getter.getAsBoolean() ? 1f : 0f);
        float t = anim.get();
        int on = enabled ? theme().accent() : Draw.alpha(theme().accent(), 110);
        int off = theme().bg3();
        if (t > 0.5f && theme().shadows()) Draw.roundedRect(ctx, x - 1, y - 1, w + 2, h + 2, h / 2 + 1, theme().accentGlow((int) (60 * t)));
        Draw.toggle(ctx, x, y, w, h, t, on, off);
        if (hovered(mx, my) && enabled) Draw.roundedBorder(ctx, x, y, w, h, h / 2, Draw.alpha(0xFFFFFFFF, 40));
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!enabled || button != 0 || !hovered(mx, my)) return false;
        playClick();
        setter.accept(!getter.getAsBoolean());
        return true;
    }
}
