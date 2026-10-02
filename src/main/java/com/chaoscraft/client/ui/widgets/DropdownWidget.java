package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.ui.Anim;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

/** Dropdown mit Popup-Liste (wird als Overlay gezeichnet). */
public class DropdownWidget extends Widget {

    private final Supplier<String> current;
    private final Supplier<List<String>> options;
    private final Consumer<Integer> onSelect;
    private boolean open;
    private final Anim openAnim = new Anim(0f, 16f);
    private static final int ROW = 16;

    public DropdownWidget(int x, int y, int w, Supplier<String> current, Supplier<List<String>> options, Consumer<Integer> onSelect) {
        super(x, y, w, 16);
        this.current = current;
        this.options = options;
        this.onSelect = onSelect;
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (!visible) return;
        boolean hov = hovered(mx, my);
        int r = Math.min(theme().radius(), 6);
        Draw.roundedRect(ctx, x, y, w, h, r, theme().bg3());
        Draw.roundedBorder(ctx, x, y, w, h, r, open || hov ? theme().accent() : theme().borderLight());
        ctx.drawTextWithShadow(font(), Draw.trim(current.get(), w - 22), x + 6, y + 4, theme().text());
        ctx.drawTextWithShadow(font(), open ? "▴" : "▾", x + w - 12, y + 4, theme().accentLight());
    }

    @Override public boolean hasOverlay() { return open || openAnim.get() > 0.01f; }

    private int popupHeight() { return options.get().size() * ROW + 4; }

    @Override
    public void renderOverlay(DrawContext ctx, int mx, int my) {
        openAnim.setTarget(open ? 1f : 0f);
        float t = openAnim.get();
        if (t < 0.01f) return;
        List<String> opts = options.get();
        int ph = (int) (popupHeight() * Anim.easeOutCubic(t));
        int py = y + h + 2;
        int r = Math.min(theme().radius(), 6);
        if (theme().shadows()) Draw.shadow(ctx, x, py, w, ph, r, 100);
        Draw.roundedRect(ctx, x, py, w, ph, r, theme().bg2());
        Draw.roundedBorder(ctx, x, py, w, ph, r, theme().accentDark());
        ctx.enableScissor(x, py, x + w, py + ph);
        for (int i = 0; i < opts.size(); i++) {
            int oy = py + 2 + i * ROW;
            boolean hov = Draw.in(mx, my, x, oy, w, ROW) && open;
            boolean sel = opts.get(i).equals(current.get());
            if (hov) Draw.roundedRect(ctx, x + 2, oy, w - 4, ROW, 4, theme().accentGlow(60));
            ctx.drawTextWithShadow(font(), Draw.trim(opts.get(i), w - 16), x + 8, oy + 4, sel ? theme().accentLight() : theme().text());
            if (sel) ctx.drawTextWithShadow(font(), "✓", x + w - 12, oy + 4, theme().accent());
        }
        ctx.disableScissor();
    }

    /** Klick-Verarbeitung für das Popup (vom Screen vor allen anderen Widgets aufgerufen). */
    public boolean overlayClicked(double mx, double my, int button) {
        if (!open) return false;
        List<String> opts = options.get();
        int py = y + h + 2;
        if (Draw.in(mx, my, x, py, w, popupHeight())) {
            int idx = (int) ((my - py - 2) / ROW);
            if (idx >= 0 && idx < opts.size() && button == 0) {
                onSelect.accept(idx);
                playClick();
            }
            open = false;
            return true;
        }
        open = false;
        return hovered(mx, my);
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!enabled || !hovered(mx, my)) return false;
        if (button == 0) { open = !open; playClick(); }
        else if (button == 1) { // Rechtsklick: nächster Wert
            List<String> opts = options.get();
            int idx = opts.indexOf(current.get());
            onSelect.accept((idx + 1) % Math.max(1, opts.size()));
        }
        return true;
    }

    public boolean isOpen() { return open; }
    public void close() { open = false; }
}
