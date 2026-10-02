package com.chaoscraft.client.ui;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.config.ChaosTheme;
import com.chaoscraft.client.ui.widgets.Widget;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Basis aller Chaos-Screens: Theme, abgedunkelter Hintergrund, Einblend-
 * Animation, Widget-Verwaltung, Tooltips, Speichern beim Schließen.
 * Die Oberfläche fühlt sich wie eine App an: Panels, Hover, Übergänge.
 */
public abstract class ChaosScreen extends Screen {

    protected final List<Widget> widgets = new ArrayList<>();
    protected final Anim intro = new Anim(0f, 9f);
    protected Screen parent;
    private String pendingTooltip;
    private int tooltipX, tooltipY;

    protected ChaosScreen(String title, Screen parent) {
        super(Text.literal(title));
        this.parent = parent;
        intro.setTarget(1f);
    }

    protected ChaosTheme theme() { return ChaosTheme.get(); }

    /** UI-Skalierung: Panelgröße relativ zum Fenster. */
    protected int panelWidth(int max) { return Math.min((int) (max * theme().scale()), width - 24); }
    protected int panelHeight(int max) { return Math.min((int) (max * theme().scale()), height - 24); }

    protected <T extends Widget> T add(T w) { widgets.add(w); return w; }

    @Override
    protected void init() {
        widgets.clear();
        intro.snap(intro.target() >= 1f && !first ? 1f : 0f);
        intro.setTarget(1f);
        first = false;
        build();
    }
    private boolean first = true;

    /** Layout aufbauen (wird bei Größenänderung erneut aufgerufen). */
    protected abstract void build();

    /** Panelhintergrund im Chaos-Stil. */
    protected void panel(DrawContext ctx, int x, int y, int w, int h) {
        int r = theme().radius();
        if (theme().shadows()) Draw.shadow(ctx, x, y, w, h, r, 120);
        Draw.roundedRect(ctx, x, y, w, h, r, theme().bg1());
        Draw.roundedBorder(ctx, x, y, w, h, r, theme().border());
        // Akzentlinie oben
        ctx.fill(x + r, y, x + w - r, y + 1, theme().accentGlow(140));
    }

    protected void header(DrawContext ctx, int x, int y, String title, String subtitle) {
        var m = ctx.getMatrices();
        m.pushMatrix();
        m.translate(x, y);
        m.scale(1.5f, 1.5f);
        ctx.drawTextWithShadow(textRenderer, "CHAOS", 0, 0, theme().accent());
        ctx.drawTextWithShadow(textRenderer, " " + title, textRenderer.getWidth("CHAOS"), 0, theme().text());
        m.popMatrix();
        if (subtitle != null) ctx.drawTextWithShadow(textRenderer, subtitle, x, y + 15, theme().textDim());
    }

    protected void setTooltip(String t, int mx, int my) { pendingTooltip = t; tooltipX = mx; tooltipY = my; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, theme().overlay());
        float t = Anim.easeOutCubic(intro.get());
        var m = ctx.getMatrices();
        m.pushMatrix();
        float sc = 0.96f + 0.04f * t;
        m.translate(width / 2f, height / 2f);
        m.scale(sc, sc);
        m.translate(-width / 2f, -height / 2f);
        renderContent(ctx, mouseX, mouseY, delta);
        for (Widget w : widgets) if (w.visible) w.render(ctx, mouseX, mouseY, delta);
        for (Widget w : widgets) if (w.visible && w.hasOverlay()) w.renderOverlay(ctx, mouseX, mouseY);
        renderOverlays(ctx, mouseX, mouseY);
        m.popMatrix();
        // Tooltip
        String tip = pendingTooltip;
        if (tip == null) for (Widget w : widgets) if (w.visible && w.hovered(mouseX, mouseY) && w.getTooltip() != null) { tip = w.getTooltip(); tooltipX = mouseX; tooltipY = mouseY; break; }
        if (tip != null && !tip.isEmpty()) drawTooltip(ctx, tip, tooltipX, tooltipY);
        pendingTooltip = null;
    }

    /** Eigene Inhalte (Panels, Texte) vor den Widgets. */
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {}
    /** Overlays nach den Widgets (z.B. ScrollPanel-Dropdowns). */
    protected void renderOverlays(DrawContext ctx, int mx, int my) {}

    /** Nur Tooltip zeichnen (für Screens mit eigenem render()). */
    protected void renderTooltipOnly(DrawContext ctx, int mouseX, int mouseY) {
        String tip = pendingTooltip;
        if (tip == null) for (Widget w : widgets) if (w.visible && w.hovered(mouseX, mouseY) && w.getTooltip() != null) { tip = w.getTooltip(); tooltipX = mouseX; tooltipY = mouseY; break; }
        if (tip != null && !tip.isEmpty()) drawTooltip(ctx, tip, tooltipX, tooltipY);
        pendingTooltip = null;
    }

    protected void drawTooltip(DrawContext ctx, String text, int mx, int my) {
        List<String> lines = new ArrayList<>();
        for (String part : text.split("\n")) {
            StringBuilder cur = new StringBuilder();
            for (String word : part.split(" ")) {
                if (textRenderer.getWidth(cur + " " + word) > 220 && cur.length() > 0) { lines.add(cur.toString()); cur = new StringBuilder(word); }
                else { if (cur.length() > 0) cur.append(' '); cur.append(word); }
            }
            lines.add(cur.toString());
        }
        int w = 0;
        for (String l : lines) w = Math.max(w, textRenderer.getWidth(l));
        w += 12;
        int h = lines.size() * 10 + 8;
        int x = Math.min(mx + 10, width - w - 4), y = my - h - 6 < 4 ? my + 14 : my - h - 6;
        if (theme().shadows()) Draw.shadow(ctx, x, y, w, h, 6, 90);
        Draw.roundedRect(ctx, x, y, w, h, 6, theme().bg2());
        Draw.roundedBorder(ctx, x, y, w, h, 6, theme().accentDark());
        for (int i = 0; i < lines.size(); i++) ctx.drawTextWithShadow(textRenderer, lines.get(i), x + 6, y + 4 + i * 10, theme().text());
    }

    /* ---------- Eingaben ---------- */
    @Override
    public boolean mouseClicked(Click click, boolean dc) {
        double mx = click.x(), my = click.y();
        int btn = click.button();
        // Offene Overlays (Dropdowns) zuerst
        for (Widget w : widgets) if (w.visible && w instanceof com.chaoscraft.client.ui.widgets.DropdownWidget dd && dd.isOpen()) { if (dd.overlayClicked(mx, my, btn)) return true; }
        // Fokussierte Keybinds fangen Maustasten
        for (Widget w : widgets) if (w.visible && w.isFocused() && w instanceof com.chaoscraft.client.ui.widgets.KeybindWidget kw) { if (kw.mouseClicked(mx, my, btn)) return true; }
        boolean handled = false;
        for (int i = widgets.size() - 1; i >= 0; i--) {
            Widget w = widgets.get(i);
            if (!w.visible) continue;
            if (!handled && w.mouseClicked(mx, my, btn)) handled = true;
            else if (!(w.hovered(mx, my))) w.setFocused(false);
        }
        if (handled) return true;
        return onClick(mx, my, btn) || super.mouseClicked(click, dc);
    }
    protected boolean onClick(double mx, double my, int button) { return false; }

    @Override
    public boolean mouseReleased(Click click) {
        boolean any = false;
        for (Widget w : widgets) if (w.visible && w.mouseReleased(click.x(), click.y(), click.button())) any = true;
        return onRelease(click.x(), click.y(), click.button()) || any || super.mouseReleased(click);
    }
    protected boolean onRelease(double mx, double my, int button) { return false; }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        for (Widget w : widgets) if (w.visible && w.mouseDragged(click.x(), click.y(), click.button(), dx, dy)) return true;
        return onDrag(click.x(), click.y(), click.button(), dx, dy) || super.mouseDragged(click, dx, dy);
    }
    protected boolean onDrag(double mx, double my, int button, double dx, double dy) { return false; }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        for (Widget w : widgets) if (w.visible && w.mouseScrolled(mx, my, v)) return true;
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    public boolean keyPressed(KeyInput key) {
        for (Widget w : widgets) if (w.visible && w.isFocused() && w.keyPressed(key)) return true;
        if (key.key() == GLFW.GLFW_KEY_ESCAPE) { close(); return true; }
        if (onKey(key)) return true;
        return super.keyPressed(key);
    }
    protected boolean onKey(KeyInput key) { return false; }

    @Override
    public boolean charTyped(CharInput chr) {
        for (Widget w : widgets) if (w.visible && w.isFocused() && w.charTyped(chr)) return true;
        return super.charTyped(chr);
    }

    @Override public boolean shouldPause() { return false; }

    @Override
    public void close() {
        ChaosClient.get().getConfigManager().save();
        if (client != null) client.setScreen(parent);
    }
}
