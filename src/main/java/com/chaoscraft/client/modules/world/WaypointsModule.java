package com.chaoscraft.client.modules.world;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.waypoints.WaypointManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

import java.util.List;

/**
 * Waypoints-HUD: Name, Entfernung, Richtungspfeil (relativ zur Blickrichtung),
 * optional als Kompassleiste. Verwaltung über den Waypoints-Screen.
 */
public class WaypointsModule extends HudModule {

    private final BooleanSetting arrow = add(new BooleanSetting("Richtungspfeil", "Pfeil relativ zur Blickrichtung.", true));
    private final BooleanSetting distance = add(new BooleanSetting("Entfernung", "Entfernung in Blöcken anzeigen.", true));
    private final IntSetting maxShown = add(new IntSetting("Max. Einträge", "Höchstens so viele Waypoints anzeigen.", 5, 1, 15));
    private final BooleanSetting compassBar = add(new BooleanSetting("Kompassleiste", "Waypoints als Markierungen auf einer Leiste (Minimap-kompatible Darstellung).", false));
    private final BooleanSetting sameServer = add(new BooleanSetting("Nur dieser Server", "Nur Waypoints anzeigen, die auf diesem Server erstellt wurden.", true));

    public WaypointsModule() {
        super("Waypoints", "Eigene Wegpunkte mit Entfernung und Richtung – gespeichert über Neustarts.", Category.WORLD, "⚑", 300, 8);
        tags("waypoint", "wegpunkt", "marker", "koordinaten", "base");
    }

    private List<WaypointManager.Waypoint> visible() {
        List<WaypointManager.Waypoint> l = ChaosClient.get().getWaypoints().forCurrentDimension();
        if (sameServer.isEnabled()) { String s = WaypointManager.currentServer(); l.removeIf(w -> !w.server.isEmpty() && !w.server.equals(s)); }
        if (mc.player != null) { Vec3d p = mc.player.getEntityPos(); l.sort((a, b) -> Double.compare(p.squaredDistanceTo(a.x, a.y, a.z), p.squaredDistanceTo(b.x, b.y, b.z))); }
        return l.size() > maxShown.getInt() ? l.subList(0, maxShown.getInt()) : l;
    }

    private String line(WaypointManager.Waypoint w) {
        StringBuilder sb = new StringBuilder();
        if (arrow.isEnabled()) sb.append(arrowFor(w)).append(' ');
        sb.append(w.name);
        if (distance.isEnabled() && mc.player != null) sb.append("  ").append((int) Math.sqrt(mc.player.getEntityPos().squaredDistanceTo(w.x + 0.5, w.y, w.z + 0.5))).append(" Blocks");
        return sb.toString();
    }

    private String arrowFor(WaypointManager.Waypoint w) {
        if (mc.player == null) return "•";
        double dx = w.x + 0.5 - mc.player.getX(), dz = w.z + 0.5 - mc.player.getZ();
        double angle = Math.toDegrees(Math.atan2(-dx, dz)) - mc.player.getYaw();
        angle = ((angle % 360) + 540) % 360 - 180;
        if (Math.abs(angle) < 22) return "↑";
        if (Math.abs(angle) > 158) return "↓";
        if (angle > 0) return angle < 68 ? "↗" : angle < 112 ? "→" : "↘";
        return angle > -68 ? "↖" : angle > -112 ? "←" : "↙";
    }

    @Override public int getContentWidth() { if (compassBar.isEnabled()) return 180; int w = 60; for (var wp : visible()) w = Math.max(w, textWidth(line(wp))); return w; }
    @Override public int getContentHeight() { if (compassBar.isEnabled()) return 22; int n = visible().size(); return n == 0 ? 10 : n * 10; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        List<WaypointManager.Waypoint> list = visible();
        if (compassBar.isEnabled()) {
            int w = 180;
            Draw.roundedRect(ctx, x, y + 9, w, 3, 1, Draw.alpha(0xFFFFFF, 50));
            ctx.fill(x + w / 2, y + 6, x + w / 2 + 1, y + 16, color());
            if (mc.player == null) return;
            for (var wp : list) {
                double dx = wp.x + 0.5 - mc.player.getX(), dz = wp.z + 0.5 - mc.player.getZ();
                double angle = Math.toDegrees(Math.atan2(-dx, dz)) - mc.player.getYaw();
                angle = ((angle % 360) + 540) % 360 - 180;
                if (Math.abs(angle) > 90) continue;
                int px = x + w / 2 + (int) (angle / 90 * (w / 2));
                Draw.roundedRect(ctx, px - 2, y + 7, 4, 7, 2, wp.color);
                String n = Draw.trim(wp.name, 50);
                ctx.drawText(mc.textRenderer, n, px - textWidth(n) / 2, y + 15, wp.color, textShadow.isEnabled());
            }
            return;
        }
        if (list.isEmpty()) { text(ctx, "Keine Waypoints", x, y, getContentWidth(), textColor.withAlpha(0.6)); return; }
        int cy = y;
        for (var wp : list) { text(ctx, line(wp), x, cy, getContentWidth(), wp.color | 0xFF000000); cy += 10; }
    }
}
