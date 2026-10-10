package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;

/** Inventory HUD: die 27 Inventar-Slots (ohne Hotbar) dauerhaft auf dem Bildschirm. */
public class InventoryHud extends HudModule {

    private final BooleanSetting slots = add(new BooleanSetting("Slot-Raster", "Hintergrund-Kästchen pro Slot zeichnen.", true));
    private final BooleanSetting hideEmpty = add(new BooleanSetting("Leer ausblenden", "HUD verstecken, wenn das Inventar leer ist.", false));

    public InventoryHud() {
        super("Inventory HUD", "Zeigt dein Inventar als Raster – ohne es zu öffnen.", Category.HUD, "▦", 8, 180);
        tags("inventar", "items", "slots");
    }

    private boolean empty() {
        if (mc.player == null) return true;
        for (int i = 9; i < 36; i++) if (!mc.player.getInventory().getStack(i).isEmpty()) return false;
        return true;
    }

    @Override public int getContentWidth() { return 9 * 18; }
    @Override public int getContentHeight() { return hideEmpty.isEnabled() && empty() ? 0 : 3 * 18; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        if (mc.player == null || (hideEmpty.isEnabled() && empty())) return;
        for (int i = 0; i < 27; i++) {
            int sx = x + (i % 9) * 18, sy = y + (i / 9) * 18;
            if (slots.isEnabled()) ctx.fill(sx, sy, sx + 18, sy + 18, 0x33FFFFFF);
            ItemStack st = mc.player.getInventory().getStack(9 + i);
            if (st.isEmpty()) continue;
            ctx.drawItem(st, sx + 1, sy + 1);
            ctx.drawStackOverlay(mc.textRenderer, st, sx + 1, sy + 1);
        }
    }
}
