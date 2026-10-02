package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

import java.util.function.Supplier;

/** Statischer oder dynamischer Text (für Panels, Statuszeilen). */
public class Label extends Widget {

    private final Supplier<String> text;
    private int color;
    private boolean centered;
    private boolean shadow = true;

    public Label(int x, int y, int w, String text, int color) { this(x, y, w, () -> text, color); }
    public Label(int x, int y, int w, Supplier<String> text, int color) {
        super(x, y, w, 10);
        this.text = text;
        this.color = color;
    }

    public Label centered() { centered = true; return this; }
    public Label noShadow() { shadow = false; return this; }
    public void setColor(int c) { color = c; }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (!visible) return;
        String s = Draw.trim(text.get(), w);
        int tx = centered ? x + (w - font().getWidth(s)) / 2 : x;
        ctx.drawText(font(), s, tx, y, color, shadow);
    }
}
