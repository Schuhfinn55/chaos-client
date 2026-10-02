package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.DoubleSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Damage Indicator / Damage Numbers: verursachter Schaden als animierte,
 * ausblendende Zahl nahe dem Fadenkreuz (CRIT-Markierung, Dezimalstellen,
 * Farbe, Dauer). Clientseitig aus Lebensänderungen des anvisierten Ziels.
 */
public class DamageIndicatorHud extends HudModule {

    private static final class Number { final String text; final long t0; final boolean crit; Number(String s, boolean c) { text = s; t0 = System.currentTimeMillis(); crit = c; } }

    private final BooleanSetting numbers = add(new BooleanSetting("Damage Numbers", "Verursachten Schaden als Zahl anzeigen.", true));
    private final IntSetting decimals = add(new IntSetting("Dezimalstellen", "Nachkommastellen.", 1, 0, 2));
    private final IntSetting duration = add(new IntSetting("Dauer", "Anzeigedauer in ms.", 900, 300, 3000));
    private final DoubleSetting rise = add(new DoubleSetting("Aufsteigen", "Pixel, die die Zahl nach oben wandert.", 18, 0, 60, 1));
    private final BooleanSetting crit = add(new BooleanSetting("Kritische Treffer", "CRIT markieren (Treffer beim Fallen).", true));
    private final ColorSetting dmgColor = add(new ColorSetting("Farbe", "Farbe normaler Treffer.", 0xFFFFFFFF, false));
    private final ColorSetting critColor = add(new ColorSetting("Crit-Farbe", "Farbe kritischer Treffer.", 0xFFE11D2E, false));
    private final BooleanSetting lastHit = add(new BooleanSetting("Letzter Treffer", "Letzten Schaden dauerhaft im HUD anzeigen.", true));
    private final BooleanSetting target = add(new BooleanSetting("Ziel-Leben", "Leben des anvisierten Ziels anzeigen.", true));

    private final Map<Integer, Float> lastHealth = new HashMap<>();
    private final List<Number> active = new ArrayList<>();
    private String last = "—";

    public DamageIndicatorHud() {
        super("Damage Indicator", "Trefferanzeige & Damage Numbers (5.0, CRIT 12.0).", Category.PVP, "✦", 300, 200);
        background.set(false);
        tags("damage", "schaden", "hit", "treffer", "numbers", "crit");
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        Iterator<Map.Entry<Integer, Float>> it = lastHealth.entrySet().iterator();
        while (it.hasNext()) if (mc.world.getEntityById(it.next().getKey()) == null) it.remove();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof LivingEntity le) || e == mc.player) continue;
            if (mc.player.squaredDistanceTo(e) > 64 * 64) continue;
            float h = le.getHealth() + le.getAbsorptionAmount();
            Float prev = lastHealth.put(e.getId(), h);
            if (prev != null && h < prev && le.hurtTime > 0 && (mc.targetedEntity == e || mc.player.squaredDistanceTo(e) < 36)) {
                float dmg = prev - h;
                boolean isCrit = crit.isEnabled() && mc.player.fallDistance > 0 && !mc.player.isOnGround();
                String s = String.format(java.util.Locale.ROOT, "%." + decimals.getInt() + "f", dmg);
                last = (isCrit ? "CRIT " : "") + s;
                if (numbers.isEnabled()) active.add(new Number(last, isCrit));
                var ch = com.chaoscraft.client.ChaosClient.get().getModuleManager().get(CrosshairModule.class);
                if (ch != null) ch.onHit();
            }
        }
    }

    @Override public int getContentWidth() { return Math.max(60, textWidth("Letzter Treffer: " + last)); }
    @Override public int getContentHeight() { return (lastHit.isEnabled() ? 10 : 0) + (target.isEnabled() ? 10 : 0); }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int cy = y;
        if (lastHit.isEnabled()) { text(ctx, "Letzter Treffer: " + last, x, cy, getContentWidth(), last.startsWith("CRIT") ? critColor.argb() : color()); cy += 10; }
        if (target.isEnabled()) {
            String t = mc.targetedEntity instanceof LivingEntity le ? Draw.trim(le.getName().getString(), 80) + " ❤ " + String.format(java.util.Locale.ROOT, "%.1f", le.getHealth()) : "Kein Ziel";
            text(ctx, t, x, cy, getContentWidth(), textColor.withAlpha(0.8));
        }
    }

    /** Schwebende Zahlen nahe dem Fadenkreuz (unabhängig von der HUD-Position). */
    @Override
    public void renderHud(DrawContext ctx) {
        super.renderHud(ctx);
        if (!numbers.isEnabled() || mc.currentScreen != null) return;
        long now = System.currentTimeMillis();
        int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2 - 20;
        Iterator<Number> it = active.iterator();
        int i = 0;
        while (it.hasNext()) {
            Number n = it.next();
            float p = (now - n.t0) / (float) duration.getInt();
            if (p >= 1f) { it.remove(); continue; }
            float ease = 1 - (1 - p) * (1 - p);
            int alpha = (int) (255 * (1 - Math.max(0, p - 0.6f) / 0.4f));
            int col = Draw.alpha(n.crit ? critColor.argb() : dmgColor.argb(), Math.max(10, alpha));
            float sc = n.crit ? 1.4f : 1.1f;
            var m = ctx.getMatrices();
            m.pushMatrix();
            m.translate(cx + 14 + (i % 2) * 10, cy - ease * rise.getFloat() - i * 4);
            m.scale(sc, sc);
            ctx.drawTextWithShadow(mc.textRenderer, n.text, 0, 0, col);
            m.popMatrix();
            i++;
        }
    }
}
