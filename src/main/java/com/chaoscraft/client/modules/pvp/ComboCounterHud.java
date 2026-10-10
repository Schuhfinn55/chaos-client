package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

/** Combo Counter: aufeinanderfolgende Treffer ohne selbst getroffen zu werden. */
public class ComboCounterHud extends HudModule {

    private static int combo, best;
    private static long lastHit;
    private static int timeoutMs = 3000;

    private final IntSetting timeout = add(new IntSetting("Timeout", "Sekunden ohne Treffer, bis die Combo endet.", 3, 1, 10));
    private final BooleanSetting resetOnHurt = add(new BooleanSetting("Reset bei Schaden", "Combo endet, wenn du getroffen wirst.", true));
    private final BooleanSetting showBest = add(new BooleanSetting("Beste Combo", "Höchste Combo der Sitzung anzeigen.", true));

    public ComboCounterHud() {
        super("Combo Counter", "Zählt deine Treffer-Serie im Kampf.", Category.PVP, "⚡", 300, 150);
        tags("combo", "treffer", "serie", "pvp");
    }

    public static void onAttack(Entity target) {
        if (!(target instanceof LivingEntity)) return;
        long now = System.currentTimeMillis();
        combo = now - lastHit < timeoutMs ? combo + 1 : 1;
        best = Math.max(best, combo);
        lastHit = now;
    }

    @Override
    public void onTick() {
        timeoutMs = timeout.getInt() * 1000;
        if (combo > 0 && System.currentTimeMillis() - lastHit > timeoutMs) combo = 0;
        if (resetOnHurt.isEnabled() && mc.player != null && mc.player.hurtTime == mc.player.maxHurtTime - 1 && mc.player.hurtTime > 0) combo = 0;
    }

    private String line() { return "Combo: " + combo; }

    @Override public int getContentWidth() { return Math.max(textWidth(line()), showBest.isEnabled() ? textWidth("Beste: " + best) : 0); }
    @Override public int getContentHeight() { return showBest.isEnabled() ? 20 : 10; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int col = combo >= 10 ? 0xFFF87171 : combo >= 5 ? 0xFFFBBF24 : color();
        text(ctx, line(), x, y, getContentWidth(), col);
        if (showBest.isEnabled()) text(ctx, "Beste: " + best, x, y + 10, getContentWidth(), textColor.withAlpha(0.7));
    }
}
