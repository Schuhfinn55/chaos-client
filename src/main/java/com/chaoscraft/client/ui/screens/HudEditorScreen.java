package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import com.chaoscraft.client.ui.widgets.SettingWidgets;
import com.chaoscraft.client.ui.widgets.ToggleWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * HUD-Editor: HUD-Elemente per Drag verschieben (mit Raster-/Kanten-Snap,
 * Hilfslinien), per Scrollrad skalieren, mit Pfeiltasten feinjustieren.
 * Das gewählte Element zeigt rechts seine Einstellungen (Größe, Rotation,
 * Transparenz, Ausrichtung, Abstände, Farbe, Hintergrund, Rahmen, Radius,
 * Schatten). Positionen werden gespeichert.
 */
public class HudEditorScreen extends ChaosScreen {

    private static boolean grid = true;
    private static int gridSize = 5;

    private HudModule selected;
    private HudModule dragging;
    private int dragOffX, dragOffY;
    private ScrollPanel sidePanel;
    private boolean sideOpen = true;
    private final List<int[]> guides = new ArrayList<>();

    public HudEditorScreen(Screen parent) { super("HUD Editor", parent); }
    public HudEditorScreen(Screen parent, HudModule select) { this(parent); this.selected = select; if (select != null && !select.isEnabled()) select.setEnabled(true); }

    private List<HudModule> huds() {
        List<HudModule> out = new ArrayList<>();
        for (HudModule h : ChaosClient.get().getModuleManager().getHudModules()) if (h.isEnabled()) out.add(h);
        return out;
    }

    @Override
    protected void build() {
        int sw = 230;
        int sx = width - sw - 8;
        add(new Button(8, height - 26, 70, 18, "Fertig", this::close).style(Button.Style.PRIMARY));
        add(new Button(82, height - 26, 60, 18, grid ? "Raster: An" : "Raster: Aus", () -> { grid = !grid; init(); }).tooltip("Elemente rasten an Raster, Kanten und Mittellinien ein"));
        add(new Button(146, height - 26, 60, 18, "Raster " + gridSize, () -> { gridSize = gridSize >= 20 ? 2 : gridSize + (gridSize < 5 ? 1 : 5); init(); }).tooltip("Rastergröße in Pixeln"));
        add(new Button(210, height - 26, 90, 18, "Positionen reset", () -> { for (HudModule h : huds()) h.setPos(10, 10); ChaosClient.get().getNotifications().info("HUD-Positionen zurückgesetzt."); }).style(Button.Style.DANGER));
        add(new Button(304, height - 26, 70, 18, sideOpen ? "Panel ‹" : "Panel ›", () -> { sideOpen = !sideOpen; init(); }));

        sidePanel = add(new ScrollPanel(sx, 30, sw, height - 64));
        sidePanel.visible = sideOpen;
        if (!sideOpen) return;
        int x = sx + 8, w = sw - 20, y = 34;
        if (selected != null) {
            y = SettingWidgets.build(sidePanel, selected, x, y, w, false);
            sidePanel.add(new Button(x, y + 4, w, 16, "Position zurücksetzen", () -> selected.setPos(10, 10)).style(Button.Style.GHOST));
            y += 24;
            sidePanel.add(new Button(x, y + 4, w, 16, "Element abwählen", () -> { selected = null; init(); }).style(Button.Style.GHOST));
            y += 24;
        } else {
            for (HudModule h : ChaosClient.get().getModuleManager().getHudModules()) {
                final HudModule hud = h;
                sidePanel.add(new com.chaoscraft.client.ui.widgets.Label(x, y + 4, w - 40, hud.getName(), hud.isSupported() ? theme().text() : theme().textFaint()));
                ToggleWidget t = new ToggleWidget(x + w - 30, y, hud::isEnabled, v -> { hud.setEnabled(v); });
                t.enabled = hud.isSupported();
                sidePanel.add(t);
                y += 20;
            }
        }
        sidePanel.setContentHeight(y + 10 - sidePanel.y);
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // leicht abdunkeln, Raster, Elemente
        ctx.fill(0, 0, width, height, Draw.alpha(0x000000, 90));
        if (grid) {
            int c = Draw.alpha(0xFFFFFF, 14);
            for (int gx = 0; gx < width; gx += gridSize * 4) ctx.fill(gx, 0, gx + 1, height, c);
            for (int gy = 0; gy < height; gy += gridSize * 4) ctx.fill(0, gy, width, gy + 1, c);
            ctx.fill(width / 2, 0, width / 2 + 1, height, Draw.alpha(0xFFFFFF, 30));
            ctx.fill(0, height / 2, width, height / 2 + 1, Draw.alpha(0xFFFFFF, 30));
        }
        for (HudModule hud : huds()) {
            hud.renderHud(ctx);
            int x = hud.getX(), y = hud.getY(), w = hud.getHudWidth(), h = hud.getHudHeight();
            boolean sel = hud == selected || hud == dragging;
            boolean hov = Draw.in(mouseX, mouseY, x, y, w, h);
            int c = sel ? theme().accent() : hov ? theme().accentLight() : Draw.alpha(0xFFFFFF, 90);
            Draw.roundedBorder(ctx, x - 2, y - 2, w + 4, h + 4, 4, c);
            if (sel || hov) {
                ctx.drawTextWithShadow(textRenderer, hud.getName() + " §7" + x + "," + y + " ×" + String.format(java.util.Locale.ROOT, "%.2f", hud.getScale()), x, y - 11, c);
            }
        }
        for (int[] g : guides) ctx.fill(g[0], g[1], g[2], g[3], theme().accentGlow(160));
        // Side-Panel Hintergrund
        if (sideOpen) {
            panel(ctx, sidePanel.x - 4, 8, sidePanel.w + 8, height - 36);
            String title = selected != null ? "✎ " + selected.getName() : "HUD-Elemente";
            ctx.drawTextWithShadow(textRenderer, title, sidePanel.x + 8, 16, theme().accentLight());
            String tip = sidePanel.tooltipAt(mouseX, mouseY);
            if (tip != null) setTooltip(tip, mouseX, mouseY);
        }
        String hint = "Ziehen: verschieben · Scrollrad: Größe · Pfeiltasten: 1px · Klick: auswählen · ESC/Fertig: speichern";
        ctx.drawTextWithShadow(textRenderer, hint, 8, height - 40, theme().textDim());
        for (var w : widgets) if (w.visible) w.render(ctx, mouseX, mouseY, delta);
        if (sidePanel != null && sidePanel.visible) sidePanel.renderOverlays(ctx, mouseX, mouseY);
        super.renderTooltipOnly(ctx, mouseX, mouseY);
    }

    @Override
    protected boolean onClick(double mx, double my, int button) {
        if (button != 0) return false;
        List<HudModule> list = huds();
        for (int i = list.size() - 1; i >= 0; i--) {
            HudModule hud = list.get(i);
            if (hud.isInside((int) mx, (int) my)) {
                boolean changed = selected != hud;
                selected = hud;
                dragging = hud;
                dragOffX = (int) mx - hud.getX();
                dragOffY = (int) my - hud.getY();
                if (changed) init();
                return true;
            }
        }
        if (selected != null && !(sideOpen && Draw.in(mx, my, sidePanel.x - 4, 8, sidePanel.w + 8, height - 36))) { selected = null; init(); return true; }
        return false;
    }

    @Override
    protected boolean onDrag(double mx, double my, int button, double dx, double dy) {
        if (dragging == null) return false;
        int nx = (int) mx - dragOffX, ny = (int) my - dragOffY;
        guides.clear();
        if (grid) {
            int w = dragging.getHudWidth(), h = dragging.getHudHeight();
            nx = Math.round(nx / (float) gridSize) * gridSize;
            ny = Math.round(ny / (float) gridSize) * gridSize;
            int snap = 6;
            // Kanten & Mitte
            int[] xs = {0, width - w, (width - w) / 2};
            int[] ys = {0, height - h, (height - h) / 2};
            for (int sx : xs) if (Math.abs(nx - sx) <= snap) { nx = sx; guides.add(new int[]{sx + (sx == 0 ? 0 : (sx == (width - w) / 2 ? w / 2 : w)), 0, sx + (sx == 0 ? 1 : (sx == (width - w) / 2 ? w / 2 + 1 : w + 1)), height}); }
            for (int sy : ys) if (Math.abs(ny - sy) <= snap) { ny = sy; guides.add(new int[]{0, sy + (sy == 0 ? 0 : (sy == (height - h) / 2 ? h / 2 : h)), width, sy + (sy == 0 ? 1 : (sy == (height - h) / 2 ? h / 2 + 1 : h + 1))}); }
            // andere Elemente
            for (HudModule o : huds()) {
                if (o == dragging) continue;
                if (Math.abs(nx - o.getX()) <= snap) { nx = o.getX(); guides.add(new int[]{nx, 0, nx + 1, height}); }
                if (Math.abs(ny - o.getY()) <= snap) { ny = o.getY(); guides.add(new int[]{0, ny, width, ny + 1}); }
                if (Math.abs((nx + w) - (o.getX() + o.getHudWidth())) <= snap) { nx = o.getX() + o.getHudWidth() - w; guides.add(new int[]{nx + w, 0, nx + w + 1, height}); }
            }
        }
        nx = Math.max(0, Math.min(width - dragging.getHudWidth(), nx));
        ny = Math.max(0, Math.min(height - dragging.getHudHeight(), ny));
        dragging.setPos(nx, ny);
        return true;
    }

    @Override
    protected boolean onRelease(double mx, double my, int button) {
        boolean was = dragging != null;
        dragging = null;
        guides.clear();
        return was;
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        if (sidePanel != null && sidePanel.visible && sidePanel.mouseScrolled(mx, my, v)) return true;
        for (HudModule hud : huds()) {
            if (hud.isInside((int) mx, (int) my)) {
                var s = (com.chaoscraft.client.settings.DoubleSetting) hud.getSetting("Scale");
                if (s != null) s.set(s.get() + (v > 0 ? 0.05 : -0.05));
                return true;
            }
        }
        return super.mouseScrolled(mx, my, h, v);
    }

    @Override
    protected boolean onKey(KeyInput key) {
        if (selected == null || (sidePanel != null && sidePanel.isFocused())) return false;
        int step = (key.modifiers() & GLFW.GLFW_MOD_SHIFT) != 0 ? gridSize : 1;
        switch (key.key()) {
            case GLFW.GLFW_KEY_LEFT -> { selected.setPos(selected.getX() - step, selected.getY()); return true; }
            case GLFW.GLFW_KEY_RIGHT -> { selected.setPos(selected.getX() + step, selected.getY()); return true; }
            case GLFW.GLFW_KEY_UP -> { selected.setPos(selected.getX(), selected.getY() - step); return true; }
            case GLFW.GLFW_KEY_DOWN -> { selected.setPos(selected.getX(), selected.getY() + step); return true; }
            default -> { return false; }
        }
    }
}
