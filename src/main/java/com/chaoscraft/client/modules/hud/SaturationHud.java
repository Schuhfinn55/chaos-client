package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

/** Saturation: Hunger und Sättigung als Zahl und Balken – sieht man in Vanilla nicht. */
public class SaturationHud extends HudModule {

    private final BooleanSetting bar = add(new BooleanSetting("Balken", "Sättigung als Balken zeichnen.", true));

    public SaturationHud() {
        super("Saturation", "Hunger und Sättigung (versteckter Vanilla-Wert) anzeigen.", Category.HUD, "♨", 8, 160);
        tags("hunger", "sättigung", "food", "essen");
    }

    private String line() {
        if (mc.player == null) return "Hunger 20 · Sättigung 5.0";
        var h = mc.player.getHungerManager();
        return String.format(java.util.Locale.ROOT, "Hunger %d · Sättigung %.1f", h.getFoodLevel(), h.getSaturationLevel());
    }

    @Override public int getContentWidth() { return Math.max(100, textWidth(line())); }
    @Override public int getContentHeight() { return bar.isEnabled() ? 17 : 10; }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        int w = getContentWidth();
        text(ctx, line(), x, y, w);
        if (bar.isEnabled() && mc.player != null) {
            float sat = Math.min(20f, mc.player.getHungerManager().getSaturationLevel());
            Draw.roundedRect(ctx, x, y + 12, w, 4, 2, 0x66000000);
            Draw.roundedRect(ctx, x, y + 12, Math.max(2, (int) (w * sat / 20f)), 4, 2, sat > 6 ? 0xFFFBBF24 : 0xFFF87171);
        }
    }
}
