package com.chaoscraft.client.ui.widgets;

import com.chaoscraft.client.ui.Anim;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;

import java.util.ArrayList;
import java.util.List;

/**
 * Scrollbarer Container mit Smooth-Scrolling, Scissor und Scrollbar.
 * Kinder werden in absoluten Koordinaten positioniert; der Panel-Offset
 * wird beim Zeichnen/Klicken angewendet.
 */
public class ScrollPanel extends Widget {

    private final List<Widget> children = new ArrayList<>();
    private int contentHeight;
    private final Anim scroll = new Anim(0f, 14f);
    private float target;
    private boolean draggingBar;

    public ScrollPanel(int x, int y, int w, int h) { super(x, y, w, h); }

    public void clear() { children.clear(); contentHeight = 0; }
    public List<Widget> children() { return children; }
    public <T extends Widget> T add(T wgt) { children.add(wgt); contentHeight = Math.max(contentHeight, wgt.y + wgt.h - y + 6); return wgt; }
    public void setContentHeight(int h) { contentHeight = h; }
    public int getContentHeight() { return contentHeight; }
    public float offset() { return scroll.get(); }
    public void resetScroll() { target = 0; scroll.snap(0); }

    private int maxScroll() { return Math.max(0, contentHeight - h); }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        if (!visible) return;
        target = Math.max(0, Math.min(maxScroll(), target));
        scroll.setTarget(target);
        int off = Math.round(scroll.get());
        ctx.enableScissor(x, y, x + w, y + h);
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(0, -off);
        int amy = my + off;
        boolean inside = Draw.in(mx, my, x, y, w, h);
        for (Widget c : children) {
            if (!c.visible) continue;
            if (c.y + c.h - off < y - 20 || c.y - off > y + h + 20) continue;
            c.render(ctx, inside ? mx : -9999, inside ? amy : -9999, delta);
        }
        m.popMatrix();
        ctx.disableScissor();
        // Scrollbar
        if (maxScroll() > 0) {
            int barH = Math.max(20, (int) ((float) h * h / contentHeight));
            int barY = y + (int) ((h - barH) * (scroll.get() / maxScroll()));
            Draw.roundedRect(ctx, x + w - 4, y, 3, h, 1, theme().bg3());
            Draw.roundedRect(ctx, x + w - 4, barY, 3, barH, 1, draggingBar ? theme().accentLight() : theme().accentDark());
        }
    }

    public void renderOverlays(DrawContext ctx, int mx, int my) {
        int off = Math.round(scroll.get());
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(0, -off);
        for (Widget c : children) if (c.visible && c.hasOverlay()) c.renderOverlay(ctx, mx, my + off);
        m.popMatrix();
    }

    private boolean overlayClick(double mx, double my, int button) {
        int off = Math.round(scroll.get());
        for (Widget c : children) {
            if (c instanceof DropdownWidget dd && dd.isOpen()) {
                return dd.overlayClicked(mx, my + off, button);
            }
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        if (!visible) return false;
        if (overlayClick(mx, my, button)) return true;
        if (!Draw.in(mx, my, x, y, w, h)) {
            for (Widget c : children) c.setFocused(false);
            return false;
        }
        if (maxScroll() > 0 && mx >= x + w - 8) { draggingBar = true; dragBar(my); return true; }
        int off = Math.round(scroll.get());
        boolean handled = false;
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget c = children.get(i);
            if (!c.visible) continue;
            if (!handled && c.mouseClicked(mx, my + off, button)) handled = true;
            else if (!(c instanceof TextField tf && tf.hovered(mx, my + off))) c.setFocused(false);
        }
        return handled || true;
    }

    private void dragBar(double my) {
        float p = (float) ((my - y) / Math.max(1, h));
        target = Math.max(0, Math.min(maxScroll(), p * contentHeight - h / 2f));
        scroll.snap(target);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (draggingBar) { dragBar(my); return true; }
        int off = Math.round(scroll.get());
        for (Widget c : children) if (c.visible && c.mouseDragged(mx, my + off, button, dx, dy)) return true;
        return false;
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        draggingBar = false;
        int off = Math.round(scroll.get());
        boolean any = false;
        for (Widget c : children) if (c.visible && c.mouseReleased(mx, my + off, button)) any = true;
        return any;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double amount) {
        if (!visible || !Draw.in(mx, my, x, y, w, h)) return false;
        int off = Math.round(scroll.get());
        for (Widget c : children) if (c.visible && c.hovered(mx, my + off) && c.mouseScrolled(mx, my + off, amount)) return true;
        target = Math.max(0, Math.min(maxScroll(), target - (float) amount * 24f));
        return true;
    }

    @Override public boolean keyPressed(KeyInput key) { for (Widget c : children) if (c.visible && c.keyPressed(key)) return true; return false; }
    @Override public boolean charTyped(CharInput chr) { for (Widget c : children) if (c.visible && c.charTyped(chr)) return true; return false; }
    @Override public boolean isFocused() { for (Widget c : children) if (c.isFocused()) return true; return false; }

    /** Tooltip des Widgets unter der Maus (oder null). */
    public String tooltipAt(double mx, double my) {
        if (!Draw.in(mx, my, x, y, w, h)) return null;
        int off = Math.round(scroll.get());
        for (int i = children.size() - 1; i >= 0; i--) {
            Widget c = children.get(i);
            if (c.visible && c.hovered(mx, my + off) && c.getTooltip() != null) return c.getTooltip();
        }
        return null;
    }
}
