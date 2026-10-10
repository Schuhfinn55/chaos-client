package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudOverlay;
import com.chaoscraft.client.core.Module;
import com.chaoscraft.client.render.WorldProjector;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.IntSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;

/** Health Indicators: Lebensbalken über Spielern und Mobs. */
public class HealthIndicatorsModule extends Module implements HudOverlay {

    private final BooleanSetting players = add(new BooleanSetting("Spieler", "Lebensbalken über Spielern.", true));
    private final BooleanSetting hostile = add(new BooleanSetting("Monster", "Lebensbalken über Monstern.", true));
    private final BooleanSetting passive = add(new BooleanSetting("Tiere", "Lebensbalken über Tieren/Dorfbewohnern.", false));
    private final BooleanSetting numbers = add(new BooleanSetting("Zahlen", "Leben als Zahl (❤ 12/20) anzeigen.", true));
    private final BooleanSetting onlyDamaged = add(new BooleanSetting("Nur verletzt", "Nur anzeigen, wenn Leben fehlt.", false));
    private final IntSetting range = add(new IntSetting("Reichweite", "Maximale Entfernung in Blöcken.", 24, 4, 64));

    public HealthIndicatorsModule() {
        super("Health Indicators", "Lebensbalken über Spielern und Mobs.", Category.PVP, "❤");
        tags("leben", "health", "balken", "mobs");
    }

    private boolean wants(LivingEntity e) {
        if (e instanceof PlayerEntity) return players.isEnabled();
        if (e instanceof HostileEntity) return hostile.isEnabled();
        if (e instanceof PassiveEntity) return passive.isEnabled();
        return hostile.isEnabled();
    }

    @Override
    public void renderOverlay(DrawContext ctx) {
        if (mc.world == null || mc.player == null || !WorldProjector.isValid()) return;
        float delta = mc.getRenderTickCounter().getTickProgress(true);
        double r2 = (double) range.getInt() * range.getInt();
        for (Entity en : mc.world.getEntities()) {
            if (!(en instanceof LivingEntity e) || e == mc.player || e.isInvisible() || !e.isAlive()) continue;
            if (e.squaredDistanceTo(mc.player) > r2 || !wants(e)) continue;
            float hp = e.getHealth(), max = Math.max(1f, e.getMaxHealth());
            if (onlyDamaged.isEnabled() && hp >= max) continue;
            var p = e.getLerpedPos(delta);
            float[] s = WorldProjector.project(p.x, p.y + e.getHeight() + 0.6, p.z);
            if (s == null) continue;
            int w = 40, x = (int) s[0] - w / 2, y = (int) s[1];
            float f = Math.min(1f, hp / max);
            int col = f > 0.5f ? 0xFF4ADE80 : f > 0.25f ? 0xFFFBBF24 : 0xFFF87171;
            Draw.roundedRect(ctx, x - 1, y - 1, w + 2, 6, 2, 0xAA000000);
            Draw.roundedRect(ctx, x, y, Math.max(1, (int) (w * f)), 4, 2, col);
            if (numbers.isEnabled()) {
                String t = "❤ " + (int) Math.ceil(hp) + "/" + (int) max;
                ctx.drawText(mc.textRenderer, t, (int) s[0] - mc.textRenderer.getWidth(t) / 2, y - 11, 0xFFFFFFFF, true);
            }
        }
    }
}
