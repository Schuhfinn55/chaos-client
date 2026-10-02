package com.chaoscraft.client.ui.screens;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.ui.ChaosScreen;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.ui.widgets.Button;
import com.chaoscraft.client.ui.widgets.Label;
import com.chaoscraft.client.ui.widgets.ScrollPanel;
import com.chaoscraft.client.ui.widgets.TextField;
import com.chaoscraft.client.ui.widgets.ToggleWidget;
import com.chaoscraft.client.waypoints.WaypointManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;

/** Waypoints: erstellen (Name, Farbe, Koordinaten), bearbeiten, an/aus, löschen. */
public class WaypointsScreen extends ChaosScreen {

    private static final int[] COLORS = {0xFFE11D2E, 0xFFFF5C6C, 0xFFF97316, 0xFFFCD34D, 0xFF4ADE80, 0xFF22D3EE, 0xFF60A5FA, 0xFFA78BFA, 0xFFF472B6, 0xFFFFFFFF};
    private ScrollPanel list;
    private String name = "";
    private String coords = "";
    private int color = COLORS[0];
    private int px, py, pw, ph;

    public WaypointsScreen(Screen parent) { super("Waypoints", parent); }

    private WaypointManager wm() { return ChaosClient.get().getWaypoints(); }

    @Override
    protected void build() {
        pw = panelWidth(620);
        ph = panelHeight(460);
        px = (width - pw) / 2;
        py = (height - ph) / 2;
        add(new Button(px + pw - 62, py + 13, 50, 20, "Zurück", this::close));
        // Formular
        add(new TextField(px + 8, py + 52, 170, 18, name, 24, v -> name = v).placeholder("Name, z.B. Chaos Base"));
        add(new TextField(px + 182, py + 52, 130, 18, coords, 32, v -> coords = v).placeholder("x y z (leer = hier)"));
        int cx = px + 316;
        for (int c : COLORS) {
            final int col = c;
            add(new Button(cx, py + 52, 14, 18, "", () -> { color = col; init(); }) {
                @Override public void render(DrawContext ctx, int mx, int my, float delta) {
                    Draw.roundedRect(ctx, x, y + 2, w, h - 4, 3, col);
                    if (color == col) Draw.roundedBorder(ctx, x - 1, y + 1, w + 2, h - 2, 4, 0xFFFFFFFF);
                }
            });
            cx += 16;
        }
        add(new Button(px + pw - 100, py + 51, 92, 20, "+ Erstellen", this::create).style(Button.Style.PRIMARY));

        list = add(new ScrollPanel(px + 8, py + 80, pw - 16, ph - 94));
        int y = list.y + 2, x = list.x + 6, w = list.w - 20;
        String dim = WaypointManager.currentDimension();
        if (wm().all().isEmpty()) list.add(new Label(x, y + 4, w, "Noch keine Waypoints. Auch per /chaos waypoint add <Name>.", theme().textFaint()));
        for (WaypointManager.Waypoint wp : wm().all()) {
            final WaypointManager.Waypoint way = wp;
            boolean here = wp.dimension.isEmpty() || wp.dimension.equals(dim);
            double dist = client.player != null ? Math.sqrt(client.player.getBlockPos().getSquaredDistance(wp.pos())) : 0;
            list.add(new Label(x + 14, y + 3, w - 160, wp.name, wp.enabled ? theme().text() : theme().textFaint()));
            list.add(new Label(x + 14, y + 13, w - 160, "X " + wp.x + "  Y " + wp.y + "  Z " + wp.z + (here ? "  §7· " + (int) dist + " Blöcke" : "  §8· " + shortDim(wp.dimension)), theme().textDim()));
            list.add(new Button(x, y + 6, 10, 10, "", () -> {}) {
                @Override public void render(DrawContext ctx, int mx, int my, float delta) { Draw.roundedRect(ctx, x, y, w, h, 3, way.color); }
                @Override public boolean mouseClicked(double mx, double my, int b) { if (!hovered(mx, my)) return false; int i = indexOf(way.color); way.color = COLORS[(i + 1) % COLORS.length]; wm().save(); return true; }
            });
            list.add(new ToggleWidget(x + w - 120, y + 5, () -> way.enabled, v -> { way.enabled = v; wm().save(); }));
            list.add(new Button(x + w - 84, y + 4, 46, 16, "Hier", () -> { if (client.player != null) { var p = client.player.getBlockPos(); way.x = p.getX(); way.y = p.getY(); way.z = p.getZ(); way.dimension = WaypointManager.currentDimension(); wm().save(); init(); } }).tooltip("Koordinaten auf aktuelle Position setzen"));
            list.add(new Button(x + w - 34, y + 4, 28, 16, "✕", () -> { wm().remove(way.id); init(); }).style(Button.Style.DANGER));
            y += 28;
        }
        list.setContentHeight(y + 6 - list.y);
    }

    private static int indexOf(int c) { for (int i = 0; i < COLORS.length; i++) if (COLORS[i] == c) return i; return 0; }
    private static String shortDim(String d) { int i = d.indexOf(':'); return i >= 0 ? d.substring(i + 1) : d; }

    private void create() {
        if (name.isBlank()) { ChaosClient.get().getNotifications().warn("Bitte einen Namen eingeben."); return; }
        String[] parts = coords.trim().split("[ ,]+");
        if (parts.length >= 3) {
            try {
                wm().add(name.trim(), color, Integer.parseInt(parts[0]), Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            } catch (NumberFormatException e) {
                ChaosClient.get().getNotifications().error("Koordinaten ungültig (x y z).");
                return;
            }
        } else {
            wm().add(name.trim(), color);
        }
        ChaosClient.get().getNotifications().success("Waypoint '" + name.trim() + "' erstellt.");
        name = "";
        coords = "";
        init();
    }

    @Override
    protected void renderContent(DrawContext ctx, int mx, int my, float delta) {
        panel(ctx, px, py, pw, ph);
        header(ctx, px + 16, py + 12, "WAYPOINTS", null);
        ctx.fill(px + 8, py + 44, px + pw - 8, py + 45, theme().border());
        String t = list.tooltipAt(mx, my);
        if (t != null) setTooltip(t, mx, my);
    }
}
