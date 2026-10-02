package com.chaoscraft.client.modules.pvp;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

/** Player Info: anvisierter Spieler mit Name, Abstand, Herzen, Rüstung, Held Item, Effekte. */
public class PlayerInfoHud extends HudModule {

    private final BooleanSetting onlyPlayers = add(new BooleanSetting("Nur Spieler", "Nur Spieler statt aller Lebewesen anzeigen.", true));
    private final BooleanSetting distance = add(new BooleanSetting("Abstand", "Entfernung anzeigen.", true));
    private final BooleanSetting hearts = add(new BooleanSetting("Herzen", "Lebensanzeige.", true));
    private final BooleanSetting armor = add(new BooleanSetting("Rüstung", "Rüstungsteile anzeigen.", true));
    private final BooleanSetting held = add(new BooleanSetting("Held Item", "Gehaltenes Item anzeigen.", true));
    private final BooleanSetting effects = add(new BooleanSetting("Effekte", "Status-Effekte anzeigen.", true));
    private final BooleanSetting keepLast = add(new BooleanSetting("Letztes Ziel halten", "Ziel 3 s nach Wegschauen weiter anzeigen.", true));

    private LivingEntity lastTarget;
    private long lastSeen;

    public PlayerInfoHud() {
        super("Player Info", "Infos zum anvisierten Spieler: Name, Abstand, Herzen, Rüstung, Item, Effekte.", Category.PVP, "☺", 300, 100);
        tags("target", "ziel", "gegner", "player", "info");
    }

    private LivingEntity target() {
        if (mc.targetedEntity instanceof LivingEntity le && (!onlyPlayers.isEnabled() || le instanceof PlayerEntity)) { lastTarget = le; lastSeen = System.currentTimeMillis(); return le; }
        if (keepLast.isEnabled() && lastTarget != null && lastTarget.isAlive() && System.currentTimeMillis() - lastSeen < 3000) return lastTarget;
        return null;
    }

    @Override public int getContentWidth() { return 120; }
    @Override public int getContentHeight() { LivingEntity t = target(); if (t == null) return 10; int h = 10; if (hearts.isEnabled()) h += 10; if (armor.isEnabled()) h += 20; if (held.isEnabled()) h += 20; if (effects.isEnabled() && !t.getStatusEffects().isEmpty()) h += 10; return h; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        LivingEntity t = target();
        int w = getContentWidth();
        if (t == null) { text(ctx, "Kein Ziel", x, y, w, textColor.withAlpha(0.6)); return; }
        int cy = y;
        String name = Draw.trim(t.getName().getString(), 80);
        String dist = distance.isEnabled() && mc.player != null ? String.format(java.util.Locale.ROOT, "%.1fm", Math.sqrt(mc.player.squaredDistanceTo(t))) : "";
        text(ctx, name, x, cy, w, accent());
        if (!dist.isEmpty()) ctx.drawText(mc.textRenderer, dist, x + w - textWidth(dist), cy, color(), textShadow.isEnabled());
        cy += 10;
        if (hearts.isEnabled()) {
            float hp = t.getHealth() + t.getAbsorptionAmount(), max = t.getMaxHealth();
            Draw.roundedRect(ctx, x, cy + 2, w, 5, 2, Draw.alpha(0xFFFFFF, 40));
            Draw.roundedRect(ctx, x, cy + 2, Math.max(2, (int) (w * Math.min(1, hp / max))), 5, 2, hp / max > 0.5f ? 0xFF4ADE80 : hp / max > 0.25f ? 0xFFFBBF24 : 0xFFF87171);
            String hs = String.format(java.util.Locale.ROOT, "%.1f ❤", hp);
            ctx.drawText(mc.textRenderer, hs, x + w - textWidth(hs), cy - 1, color(), textShadow.isEnabled());
            cy += 10;
        }
        if (armor.isEnabled()) {
            int ix = x;
            for (EquipmentSlot s : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
                ItemStack st = t.getEquippedStack(s);
                if (!st.isEmpty()) ctx.drawItem(st, ix, cy); else ctx.fill(ix, cy, ix + 16, cy + 16, 0x33000000);
                ix += 18;
            }
            cy += 20;
        }
        if (held.isEnabled()) {
            ItemStack main = t.getMainHandStack();
            if (!main.isEmpty()) { ctx.drawItem(main, x, cy); ctx.drawText(mc.textRenderer, Draw.trim(main.getName().getString(), w - 22), x + 20, cy + 4, color(), textShadow.isEnabled()); }
            else ctx.drawText(mc.textRenderer, "Keine Hand", x, cy + 4, textColor.withAlpha(0.6), textShadow.isEnabled());
            cy += 20;
        }
        if (effects.isEnabled() && !t.getStatusEffects().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (StatusEffectInstance e : t.getStatusEffects()) { if (sb.length() > 0) sb.append(", "); sb.append(e.getEffectType().value().getName().getString()); }
            text(ctx, Draw.trim(sb.toString(), w), x, cy, w, textColor.withAlpha(0.8));
        }
    }
}
