package com.chaoscraft.client.modules.hud;

import com.chaoscraft.client.core.Category;
import com.chaoscraft.client.core.HudModule;
import com.chaoscraft.client.settings.BooleanSetting;
import net.minecraft.client.gui.DrawContext;

/** Day Counter: Welttag der aktuellen Welt/des Servers. */
public class DayCounterHud extends HudModule {

    private final BooleanSetting showTime = add(new BooleanSetting("Tageszeit", "Zusätzlich die Minecraft-Uhrzeit anzeigen.", false));

    public DayCounterHud() {
        super("Day Counter", "Zeigt, am wievielten Tag die Welt ist.", Category.HUD, "☀", 8, 130);
        tags("tag", "day", "welt", "zeit");
    }

    private String line() {
        if (mc.world == null) return "Tag 0";
        long t = mc.world.getTimeOfDay();
        String s = "Tag " + (t / 24000 + 1);
        if (showTime.isEnabled()) {
            long d = (t % 24000 + 6000) % 24000;
            s += String.format(java.util.Locale.ROOT, " · %02d:%02d", d / 1000, (d % 1000) * 60 / 1000);
        }
        return s;
    }

    @Override public int getContentWidth() { return textWidth(line()); }
    @Override public int getContentHeight() { return 10; }
    @Override public void renderContent(DrawContext ctx, int x, int y) { text(ctx, line(), x, y, getContentWidth()); }
}
