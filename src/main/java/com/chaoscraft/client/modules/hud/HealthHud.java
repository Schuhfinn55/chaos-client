package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import com.chaoscraft.client.settings.ColorSetting;
import com.chaoscraft.client.settings.EnumSetting;
import com.chaoscraft.client.ui.Draw;
import net.minecraft.client.gui.DrawContext;

/** Eigene Lebensanzeige: Herzleiste, Prozent, Zahl, Balken, Custom. */
public class HealthHud extends HudModule {

    public enum Variant { HEARTS, PERCENT, NUMBER, BAR, CUSTOM }

    private final EnumSetting<Variant> variant = add(new EnumSetting<>("Variante", "Darstellung der Lebensanzeige.", Variant.NUMBER));
    private final BooleanSetting absorption = add(new BooleanSetting("Absorption", "Zusatzherzen einrechnen.", true));
    private final BooleanSetting hunger = add(new BooleanSetting("Hunger", "Hunger ebenfalls anzeigen.", false));
    private final ColorSetting healthColor = add(new ColorSetting("Farbe", "Farbe der Anzeige.", 0xFFE11D2E, false));
    private final BooleanSetting lowWarn = add(new BooleanSetting("Warnung", "Bei wenig Leben blinken.", true));

    public HealthHud() {
        super("Health Bar", "Eigene Lebensanzeige: ❤ 20 / 20, Prozent, Balken …", Category.HUD, "❤", 300, 330);
        tags("health", "leben", "herzen", "hp", "hunger");
    }

    private float hp() { return mc.player == null ? 0 : mc.player.getHealth() + (absorption.isEnabled() ? mc.player.getAbsorptionAmount() : 0); }
    private float max() { return mc.player == null ? 20 : mc.player.getMaxHealth(); }

    private String text() {
        float h = hp(), m = max();
        return switch (variant.get()) {
            case PERCENT -> Math.round(h / m * 100) + "%";
            case HEARTS -> "";
            case BAR -> "";
            case CUSTOM -> String.format(java.util.Locale.ROOT, "%.1f ❤", h);
            default -> "❤ " + Math.round(h) + " / " + Math.round(m);
        };
    }

    @Override public int getContentWidth() { return switch (variant.get()) { case HEARTS -> Math.min(10, (int) Math.ceil(max() / 2)) * 9; case BAR -> 80; default -> Math.max(textWidth(text()), hunger.isEnabled() ? 60 : 0); }; }
    @Override public int getContentHeight() { return 10 + (hunger.isEnabled() ? 10 : 0); }

    @Override
    public void renderContent(DrawContext ctx, int x, int y) {
        float h = hp(), m = max();
        boolean low = lowWarn.isEnabled() && h <= m * 0.25f && (System.currentTimeMillis() / 350) % 2 == 0;
        int col = low ? 0xFFFFFFFF : healthColor.argb();
        int w = getContentWidth();
        switch (variant.get()) {
            case HEARTS -> {
                int hearts = (int) Math.ceil(m / 2);
                for (int i = 0; i < Math.min(10, hearts); i++) {
                    float v = h / 2 - i;
                    int c = v >= 1 ? col : v > 0 ? Draw.alpha(col, 160) : Draw.alpha(0xFFFFFF, 50);
                    ctx.drawText(mc.textRenderer, "❤", x + i * 9, y, c, textShadow.isEnabled());
                }
            }
            case BAR -> {
                Draw.roundedRect(ctx, x, y + 2, w, 6, 3, Draw.alpha(0xFFFFFF, 40));
                Draw.roundedRect(ctx, x, y + 2, Math.max(3, (int) (w * Math.min(1f, h / m))), 6, 3, col);
            }
            default -> text(ctx, text(), x, y, w, col);
        }
        if (hunger.isEnabled() && mc.player != null) {
            int f = mc.player.getHungerManager().getFoodLevel();
            text(ctx, "🍗 " + f + " / 20", x, y + 10, w, 0xFFF97316);
        }
    }
}
