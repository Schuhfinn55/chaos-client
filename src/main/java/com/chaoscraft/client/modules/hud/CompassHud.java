package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.ChaosClient;
import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.ui.Draw;
import com.chaoscraft.client.waypoints.WaypointManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;

/** Kompass-Leiste: Himmelsrichtungen und Waypoint-Markierungen relativ zur Blickrichtung. */
public class CompassHud extends HudModule {

    private final IntSetting width = add(new IntSetting("Breite", "Breite der Leiste in Pixeln.", 180, 100, 400));
    private final IntSetting fov = add(new IntSetting("Sichtfeld", "Wie viele Grad die Leiste abdeckt.", 180, 90, 360));
    private final BooleanSetting waypoints = add(new BooleanSetting("Waypoints", "Waypoints als Markierungen anzeigen.", true));
    private final BooleanSetting degrees = add(new BooleanSetting("Gradzahl", "Blickrichtung in Grad anzeigen.", true));

    public CompassHud() {
        super("Compass", "Kompass-Leiste mit Himmelsrichtungen und Waypoints.", Category.HUD, "✧", 200, 4);
        background.set(false);
        tags("kompass", "richtung", "norden", "waypoints");
    }

    private static float heading() {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return 0;
        return (MathHelper.wrapDegrees(mc.player.getYaw()) + 180f + 360f) % 360f;
    }

    private static float headingOf(double dx, double dz) {
        return (float) ((Math.toDegrees(Math.atan2(-dx, dz)) + 180.0 + 360.0) % 360.0);
    }

    @Override public int getContentWidth() { return width.getInt(); }
    @Override public int getContentHeight() { return degrees.isEnabled() ? 24 : 16; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int w = width.getInt(), cx = x + w / 2;
        float half = fov.getInt() / 2f;
        float head = heading();
        Draw.roundedRect(ctx, x, y, w, 14, 4, 0x80000000);
        // Ticks und Richtungen
        String[] names = {"N", "NO", "O", "SO", "S", "SW", "W", "NW"};
        for (int a = 0; a < 360; a += 15) {
            float rel = MathHelper.wrapDegrees(a - head);
            if (Math.abs(rel) > half) continue;
            int px = (int) (cx + rel / half * (w / 2f - 4));
            boolean major = a % 45 == 0;
            ctx.fill(px, y + (major ? 2 : 6), px + 1, y + 12, major ? 0xFFFFFFFF : 0x80FFFFFF);
            if (major) {
                String n = names[a / 45];
                int col = a == 0 ? 0xFFF87171 : color();
                ctx.drawText(mc.textRenderer, n, px - textWidth(n) / 2, y + 15, col, textShadow.isEnabled());
            }
        }
        // Waypoints
        if (waypoints.isEnabled() && mc.player != null && ChaosClient.get() != null) {
            for (WaypointManager.Waypoint wp : ChaosClient.get().getWaypoints().forCurrentDimension()) {
                if (!wp.enabled) continue;
                double dx = wp.x + 0.5 - mc.player.getX(), dz = wp.z + 0.5 - mc.player.getZ();
                float rel = MathHelper.wrapDegrees(headingOf(dx, dz) - head);
                if (Math.abs(rel) > half) continue;
                int px = (int) (cx + rel / half * (w / 2f - 4));
                ctx.fill(px - 1, y + 3, px + 2, y + 11, wp.color | 0xFF000000);
                String n = Draw.trim(wp.name, 40);
                ctx.drawText(mc.textRenderer, n, px - textWidth(n) / 2, y - 9, wp.color | 0xFF000000, true);
            }
        }
        // Mittelmarker + Grad
        ctx.fill(cx, y, cx + 1, y + 14, accent());
        if (degrees.isEnabled()) {
            String d = Math.round(head) + "°";
            ctx.drawText(mc.textRenderer, d, cx - textWidth(d) / 2, y + 15, color(), textShadow.isEnabled());
        }
    }
}
