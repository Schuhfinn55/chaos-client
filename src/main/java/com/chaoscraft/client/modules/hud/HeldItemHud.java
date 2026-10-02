package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.ui.Anim;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/** Held Item HUD: Name, Anzahl, Haltbarkeit, Icon, Wechsel-Animation. */
public class HeldItemHud extends HudModule {

    private final BooleanSetting showName = add(new BooleanSetting("Itemname", "Namen anzeigen.", true));
    private final BooleanSetting showCount = add(new BooleanSetting("Anzahl", "Stapelgröße anzeigen.", true));
    private final BooleanSetting showDurability = add(new BooleanSetting("Haltbarkeit", "Haltbarkeit als Zahl + Balken.", true));
    private final BooleanSetting icon = add(new BooleanSetting("Icon", "Item-Icon anzeigen.", true));
    private final BooleanSetting animate = add(new BooleanSetting("Animation", "Kurzes Aufblenden beim Itemwechsel.", true));

    private ItemStack last = ItemStack.EMPTY;
    private final Anim pop = new Anim(1f, 10f);

    public HeldItemHud() {
        super("Held Item", "Zeigt das aktuell gehaltene Item mit Haltbarkeit.", Category.HUD, "✋", 300, 300);
        tags("item", "hand", "durability", "held");
    }

    private ItemStack stack() { return mc.player == null ? ItemStack.EMPTY : mc.player.getMainHandStack(); }

    @Override public int getContentWidth() { ItemStack s = stack(); int w = icon.isEnabled() ? 20 : 0; int tw = 60; if (!s.isEmpty()) { tw = Math.max(textWidth(s.getName().getString().toUpperCase()), showDurability.isEnabled() && s.isDamageable() ? textWidth("Durability: " + (s.getMaxDamage() - s.getDamage()) + " / " + s.getMaxDamage()) : 0); } return w + tw; }
    @Override public int getContentHeight() { return 20; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        ItemStack s = stack();
        if (!ItemStack.areItemsEqual(s, last)) { last = s.copy(); if (animate.isEnabled()) { pop.snap(0.6f); pop.setTarget(1f); } }
        float sc = animate.isEnabled() ? pop.get() : 1f;
        if (s.isEmpty()) { text(ctx, "Keine Hand", x + (icon.isEnabled() ? 20 : 0), y + 5, 60, textColor.withAlpha(0.6)); return; }
        if (icon.isEnabled()) {
            var m = ctx.getMatrices();
            m.pushMatrix();
            m.translate(x + 8, y + 10);
            m.scale(sc, sc);
            ctx.drawItem(s, -8, -8);
            if (showCount.isEnabled() && s.getCount() > 1) ctx.drawStackOverlay(mc.textRenderer, s, -8, -8);
            m.popMatrix();
        }
        int tx = x + (icon.isEnabled() ? 20 : 0);
        if (showName.isEnabled()) ctx.drawText(mc.textRenderer, Draw.trim(s.getName().getString().toUpperCase(), 140), tx, y + 1, color(), textShadow.isEnabled());
        if (showDurability.isEnabled() && s.isDamageable()) {
            int max = s.getMaxDamage(), left = max - s.getDamage();
            float r = max > 0 ? (float) left / max : 0;
            int col = r > 0.5f ? 0xFF4ADE80 : r > 0.25f ? 0xFFFBBF24 : 0xFFF87171;
            ctx.drawText(mc.textRenderer, "Durability: " + left + " / " + max, tx, y + 11, col, textShadow.isEnabled());
        } else if (showCount.isEnabled() && !icon.isEnabled()) {
            ctx.drawText(mc.textRenderer, "x" + s.getCount(), tx, y + 11, color(), textShadow.isEnabled());
        }
    }
}
