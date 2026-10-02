package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.EnumSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** Koordinaten-HUD: XYZ, Blickrichtung, Himmelsrichtung, Dimension, Biome, Geschwindigkeit. */
public class CoordinatesHud extends HudModule {

    public enum Layout { LINES, INLINE }

    private final EnumSetting<Layout> layout = add(new EnumSetting<>("Layout", "Zeilenweise oder in einer Zeile.", Layout.LINES));
    private final BooleanSetting direction = add(new BooleanSetting("Blickrichtung", "Yaw/Pitch anzeigen.", false));
    private final BooleanSetting compass = add(new BooleanSetting("Himmelsrichtung", "N/O/S/W anzeigen.", true));
    private final BooleanSetting dimension = add(new BooleanSetting("Dimension", "Aktuelle Dimension anzeigen.", false));
    private final BooleanSetting biome = add(new BooleanSetting("Biome", "Aktuelles Biom anzeigen.", true));
    private final BooleanSetting speed = add(new BooleanSetting("Geschwindigkeit", "Bewegung in Blöcken/Sekunde.", false));

    private Vec3d lastPos = Vec3d.ZERO;
    private double bps;

    public CoordinatesHud() {
        super("Coordinates", "Zeigt X/Y/Z, Richtung, Biom und mehr.", Category.HUD, "⌖", 8, 150);
        tags("coords", "koordinaten", "xyz", "position", "biome", "kompass");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        Vec3d p = mc.player.getEntityPos();
        double d = Math.sqrt(Math.pow(p.x - lastPos.x, 2) + Math.pow(p.z - lastPos.z, 2));
        bps = bps * 0.7 + (d * 20) * 0.3;
        lastPos = p;
    }

    private List<String> lines() {
        List<String> l = new ArrayList<>();
        if (mc.player == null) return l;
        BlockPos p = mc.player.getBlockPos();
        if (layout.get() == Layout.INLINE) l.add("X " + p.getX() + "  Y " + p.getY() + "  Z " + p.getZ());
        else { l.add("X: " + p.getX()); l.add("Y: " + p.getY()); l.add("Z: " + p.getZ()); }
        if (compass.isEnabled()) {
            Direction d = Direction.fromHorizontalDegrees(mc.player.getYaw());
            String n = switch (d) { case NORTH -> "Norden (−Z)"; case SOUTH -> "Süden (+Z)"; case EAST -> "Osten (+X)"; case WEST -> "Westen (−X)"; default -> d.asString(); };
            l.add(n);
        }
        if (direction.isEnabled()) l.add(String.format(java.util.Locale.ROOT, "Yaw %.1f  Pitch %.1f", wrap(mc.player.getYaw()), mc.player.getPitch()));
        if (dimension.isEnabled() && mc.world != null) l.add(dimName(mc.world.getRegistryKey().getValue().getPath()));
        if (biome.isEnabled() && mc.world != null) {
            String b = mc.world.getBiome(p).getKey().map(k -> k.getValue().getPath()).orElse("?");
            l.add(pretty(b));
        }
        if (speed.isEnabled()) l.add(String.format(java.util.Locale.ROOT, "%.1f b/s", bps));
        return l;
    }

    private static float wrap(float yaw) { yaw %= 360; if (yaw < -180) yaw += 360; if (yaw > 180) yaw -= 360; return yaw; }
    private static String dimName(String p) { return switch (p) { case "overworld" -> "Oberwelt"; case "the_nether" -> "Nether"; case "the_end" -> "Ende"; default -> pretty(p); }; }
    private static String pretty(String s) {
        StringBuilder sb = new StringBuilder();
        for (String part : s.split("_")) { if (part.isEmpty()) continue; if (sb.length() > 0) sb.append(' '); sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)); }
        return sb.toString();
    }

    @Override public int getContentWidth() { int w = 40; for (String s : lines()) w = Math.max(w, textWidth(s)); return w; }
    @Override public int getContentHeight() { return Math.max(1, lines().size()) * 10; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int w = getContentWidth();
        int cy = y;
        for (String s : lines()) { text(ctx, s, x, cy, w); cy += 10; }
    }
}
