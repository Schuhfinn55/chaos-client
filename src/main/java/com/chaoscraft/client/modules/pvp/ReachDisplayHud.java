package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.IntSetting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.hit.EntityHitResult;

/** Reach Display: Entfernung deines letzten Treffers (Blöcke). */
public class ReachDisplayHud extends HudModule {

    private static double last = -1, best;
    private static long lastAt;

    private final IntSetting decimals = add(new IntSetting("Dezimalstellen", "Nachkommastellen.", 2, 1, 3));
    private final BooleanSetting showBest = add(new BooleanSetting("Maximum", "Größte Reichweite der Sitzung anzeigen.", true));
    private final IntSetting hideAfter = add(new IntSetting("Ausblenden nach", "Sekunden, bis der Wert auf – zurückfällt (0 = nie).", 0, 0, 30));

    public ReachDisplayHud() {
        super("Reach Display", "Wie weit dein letzter Treffer entfernt war.", Category.PVP, "↔", 300, 175);
        tags("reach", "reichweite", "distanz", "pvp");
    }

    public static void onAttack(EntityHitResult hit) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        last = mc.player.getEyePos().distanceTo(hit.getPos());
        best = Math.max(best, last);
        lastAt = System.currentTimeMillis();
    }

    private String line() {
        if (last < 0 || (hideAfter.getInt() > 0 && System.currentTimeMillis() - lastAt > hideAfter.getInt() * 1000L)) return "Reach: –";
        return String.format(java.util.Locale.ROOT, "Reach: %." + decimals.getInt() + "f", last);
    }

    @Override public int getContentWidth() { return Math.max(textWidth(line()), showBest.isEnabled() ? textWidth(String.format(java.util.Locale.ROOT, "Max: %." + decimals.getInt() + "f", best)) : 0); }
    @Override public int getContentHeight() { return showBest.isEnabled() ? 20 : 10; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        text(ctx, line(), x, y, getContentWidth());
        if (showBest.isEnabled()) text(ctx, String.format(java.util.Locale.ROOT, "Max: %." + decimals.getInt() + "f", best), x, y + 10, getContentWidth(), textColor.withAlpha(0.7));
    }
}
