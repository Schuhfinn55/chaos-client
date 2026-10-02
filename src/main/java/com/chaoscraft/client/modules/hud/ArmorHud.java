package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.settings.IntSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;

/** Armor HUD: Helm, Brustplatte, Hose, Schuhe (+ Hand), Haltbarkeit, Prozent, Name, Warnung. */
public class ArmorHud extends HudModule {

    public enum Orientation { VERTICAL, HORIZONTAL }
    public enum Info { PERCENT, DURABILITY, BOTH, NONE }

    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
    private static final int SLOT = 18;

    private final EnumSetting<Orientation> orientation = add(new EnumSetting<>("Ausrichtung", "Vertikal oder horizontal.", Orientation.VERTICAL));
    private final EnumSetting<Info> info = add(new EnumSetting<>("Info", "Haltbarkeit als Prozent, Zahl oder beides.", Info.PERCENT));
    private final BooleanSetting showName = add(new BooleanSetting("Item-Name", "Namen anzeigen (nur vertikal).", false));
    private final BooleanSetting showHand = add(new BooleanSetting("Hand-Item", "Haupthand ebenfalls anzeigen.", true));
    private final BooleanSetting hideEmpty = add(new BooleanSetting("Leere ausblenden", "Leere Slots nicht zeichnen.", false));
    private final IntSetting warnAt = add(new IntSetting("Warnung ab %", "Unter diesem Wert rot blinken.", 15, 0, 50));

    public ArmorHud() {
        super("Armor HUD", "Rüstung und Werkzeug mit Haltbarkeit im Blick.", Category.HUD, "⛨", 8, 200);
        tags("armor", "rüstung", "durability", "haltbarkeit");
    }

    private int count() { return SLOTS.length + (showHand.isEnabled() ? 1 : 0); }
    private int infoWidth() { return info.get() == Info.NONE ? 0 : info.get() == Info.BOTH ? 60 : 34; }
    private int extraW() { return infoWidth() + (showName.isEnabled() && orientation.get() == Orientation.VERTICAL ? 60 : 0); }

    @Override public int getContentWidth() { return orientation.get() == Orientation.VERTICAL ? SLOT + 2 + extraW() : count() * (SLOT + 2) + (info.get() == Info.NONE ? 0 : 0); }
    @Override public int getContentHeight() { return orientation.get() == Orientation.VERTICAL ? count() * SLOT : SLOT + (info.get() == Info.NONE ? 0 : 10); }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        if (mc.player == null) return;
        int i = 0;
        for (EquipmentSlot s : SLOTS) { drawSlot(ctx, mc.player.getEquippedStack(s), x, y, i++); }
        if (showHand.isEnabled()) drawSlot(ctx, mc.player.getMainHandStack(), x, y, i);
    }

    private void drawSlot(DrawContext ctx, ItemStack stack, int x, int y, int idx) {
        boolean vertical = orientation.get() == Orientation.VERTICAL;
        int sx = vertical ? x : x + idx * (SLOT + 2);
        int sy = vertical ? y + idx * SLOT : y;
        if (stack == null || stack.isEmpty()) {
            if (!hideEmpty.isEnabled()) ctx.fill(sx, sy, sx + SLOT - 2, sy + SLOT - 2, 0x44000000);
            return;
        }
        ctx.drawItem(stack, sx, sy);
        if (!stack.isDamageable()) return;
        int max = stack.getMaxDamage(), left = Math.max(0, max - stack.getDamage());
        float ratio = max > 0 ? (float) left / max : 0f;
        int pct = Math.round(ratio * 100);
        boolean warn = pct <= warnAt.getInt() && (System.currentTimeMillis() / 400) % 2 == 0;
        int col = warn ? 0xFFFF2222 : ratio > 0.5f ? 0xFF4ADE80 : ratio > 0.25f ? 0xFFFBBF24 : 0xFFF87171;
        ctx.fill(sx + 1, sy + 14, sx + 14, sy + 15, 0xFF000000);
        ctx.fill(sx + 1, sy + 14, sx + 1 + (int) (13 * ratio), sy + 15, col);
        String txt = switch (info.get()) { case DURABILITY -> left + "/" + max; case BOTH -> pct + "% " + left; case NONE -> ""; default -> pct + "%"; };
        if (vertical) {
            if (!txt.isEmpty()) ctx.drawText(mc.textRenderer, txt, sx + SLOT + 2, sy + 5, col, textShadow.isEnabled());
            if (showName.isEnabled()) ctx.drawText(mc.textRenderer, com.chaoscraft.client.ui.Draw.trim(stack.getName().getString(), 58), sx + SLOT + 2 + infoWidth(), sy + 5, color(), textShadow.isEnabled());
        } else if (!txt.isEmpty()) {
            ctx.drawText(mc.textRenderer, txt, sx, sy + SLOT + 1, col, textShadow.isEnabled());
        }
    }
}
