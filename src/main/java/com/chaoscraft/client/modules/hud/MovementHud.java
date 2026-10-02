package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.EnumSetting;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

/** Movement-HUD: Sprinting, Sneak, Geschwindigkeit, Sprung-Status. */
public class MovementHud extends HudModule {

    public enum Format { WORD, UPPER, SHORT }

    private final BooleanSetting sprint = add(new BooleanSetting("Sprinting", "Sprint-Status anzeigen.", true));
    private final BooleanSetting sneak = add(new BooleanSetting("Sneak", "Schleich-Status anzeigen.", true));
    private final BooleanSetting speed = add(new BooleanSetting("Geschwindigkeit", "Blöcke pro Sekunde.", false));
    private final BooleanSetting jump = add(new BooleanSetting("Sprung", "Anzeigen, wenn in der Luft.", false));
    private final EnumSetting<Format> format = add(new EnumSetting<>("Format", "„Sprinting“, „SPRINT“ oder kurz.", Format.WORD));

    private double bps;
    private double lastX, lastZ;

    public MovementHud() {
        super("Movement", "Sprint-, Sneak-, Sprung-Status und Geschwindigkeit.", Category.HUD, "➶", 8, 240);
        tags("sprint", "sneak", "movement", "bewegung", "speed");
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        double dx = mc.player.getX() - lastX, dz = mc.player.getZ() - lastZ;
        bps = bps * 0.7 + Math.sqrt(dx * dx + dz * dz) * 20 * 0.3;
        lastX = mc.player.getX(); lastZ = mc.player.getZ();
    }

    private String word(String full, String upper, String shortS) { return switch (format.get()) { case UPPER -> upper; case SHORT -> shortS; default -> full; }; }

    private List<String> lines() {
        List<String> l = new ArrayList<>();
        if (mc.player == null) return l;
        if (sprint.isEnabled() && mc.player.isSprinting()) l.add(word("Sprinting", "SPRINT", "SPR"));
        if (sneak.isEnabled() && mc.player.isSneaking()) l.add(word("Sneaking", "SNEAK", "SNK"));
        if (jump.isEnabled() && !mc.player.isOnGround()) l.add(word("Jumping", "JUMP", "JMP"));
        if (speed.isEnabled()) l.add(String.format(java.util.Locale.ROOT, "%.1f b/s", bps));
        if (l.isEmpty()) l.add(word("Idle", "IDLE", "—"));
        return l;
    }

    @Override public int getContentWidth() { int w = 40; for (String s : lines()) w = Math.max(w, textWidth(s)); return w; }
    @Override public int getContentHeight() { return lines().size() * 10; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int w = getContentWidth(), cy = y;
        for (String s : lines()) { text(ctx, s, x, cy, w, s.startsWith("Spr") || s.startsWith("SPR") ? accent() : color()); cy += 10; }
    }
}
